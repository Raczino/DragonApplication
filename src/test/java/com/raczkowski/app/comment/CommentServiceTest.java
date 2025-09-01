package com.raczkowski.app.comment;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dtoMappers.CommentDtoMapper;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.likes.CommentLike;
import com.raczkowski.app.likes.CommentLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserService userService;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private CommentRequestValidator commentRequestValidator;
    @Mock
    private FeatureLimitHelperService featureLimitHelperService;
    @Mock
    private CommentDtoMapper commentDtoMapper;

    @InjectMocks
    private CommentService commentService;

    private AppUser user(long id) {
        AppUser u = new AppUser();
        u.setId(id);
        return u;
    }

    private Comment comment(long id) {
        Comment c = new Comment();
        c.setId(id);
        Article a = new Article();
        a.setId(111L);
        c.setArticle(a);
        c.setAppUser(user(99L));
        return c;
    }

    @Test
    public void shouldGetCommentsForArticleWithPaginationAndLikedFlag() {
        // Given
        AppUser user = new AppUser();
        user.setId(1L);
        when(userService.getLoggedUser()).thenReturn(user);

        Comment c1 = new Comment();
        c1.setId(10L);
        Comment c2 = new Comment();
        c2.setId(11L);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("likesCount")));
        Page<Comment> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        try (MockedStatic<GenericService> mocked = mockStatic(GenericService.class)) {
            mocked.when(() -> GenericService.paginate(eq(1), eq(5), eq("likesCount"), eq("DESC"), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Function<Pageable, Page<Comment>> supplier =
                                inv.getArgument(4);

                        when(commentRepository.findCommentsByArticleWithPinnedFirst(eq(100L), any(Pageable.class)))
                                .thenReturn(page);
                        return supplier.apply(pageable);
                    });

            when(commentLikeRepository.findLikedCommentIdsByUserAndCommentIds(eq(user), anyList()))
                    .thenReturn(Set.of(11L));

            CommentDto d1 = new CommentDto();
            CommentDto d2 = new CommentDto();
            when(commentDtoMapper.toCommentDto(c1)).thenReturn(d1);
            when(commentDtoMapper.toCommentDto(c2)).thenReturn(d2);

            // When
            PageResponse<CommentDto> out = commentService.getCommentsForArticle(100L, 1, 5);

            // Then
            assertEquals(2, out.getItems().size());
            assertFalse(out.getItems().get(0).isLiked());
            assertTrue(out.getItems().get(1).isLiked());
            MetaData meta = out.getMeta();
            assertEquals(2, meta.getTotalItems());
            assertEquals(1, meta.getTotalPages());
            assertEquals(1, meta.getCurrentPage());
            assertEquals(5, meta.getPageSize());
        }
    }

    @Test
    public void shouldAddCommentAndUpdateCounters() {
        // Given
        AppUser user = new AppUser();
        user.setId(5L);
        when(userService.getLoggedUser()).thenReturn(user);

        CommentRequest req = new CommentRequest();
        req.setId(200L);
        req.setContent("hello");

        when(articleRepository.existsById(200L)).thenReturn(true);

        Article article = new Article();
        article.setId(200L);
        when(articleRepository.findArticleById(200L)).thenReturn(article);

        CommentDto dto = new CommentDto();
        when(commentDtoMapper.toCommentDto(any(Comment.class))).thenReturn(dto);

        // When
        CommentDto out = commentService.addComment(req);

        // Then
        assertSame(dto, out);
        verify(commentRequestValidator).validateCreationRequest(eq(req), eq(user));
        verify(commentRepository).save(any(Comment.class));
        verify(articleRepository).updateArticleLikesCount(200L, 1);
        verify(featureLimitHelperService).incrementFeatureUsage(5L, FeatureKeys.COMMENT_COUNT_PER_WEEK);
    }

    @Test
    public void shouldThrowWhenAddCommentArticleNotExists() {
        // Given
        AppUser user = new AppUser();
        user.setId(5L);
        when(userService.getLoggedUser()).thenReturn(user);

        CommentRequest req = new CommentRequest();
        req.setId(201L);
        req.setContent("x");

        when(articleRepository.existsById(201L)).thenReturn(false);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.addComment(req));
        assertEquals(ErrorMessages.ARTICLE_NOT_EXISTS, ex.getMessage());

        verify(commentRepository, never()).save(any());
        verify(articleRepository, never()).updateArticleLikesCount(anyLong(), anyInt());
        verify(featureLimitHelperService, never()).incrementFeatureUsage(anyLong(), anyString());
    }

    @Test
    public void shouldCreateComment() {
        // Given
        Comment c = new Comment();
        // When
        commentService.createComment(c);
        // Then
        verify(commentRepository).save(c);
    }

    @Test
    public void shouldAddLikeAndIncreaseCount() {
        // Given
        AppUser user = new AppUser();
        user.setId(1L);
        when(userService.getLoggedUser()).thenReturn(user);

        Comment c = new Comment();
        c.setId(10L);
        when(commentRepository.findCommentById(10L)).thenReturn(c);

        when(commentLikeRepository.existsCommentLikeByAppUserAndComment(eq(user), eq(c)))
                .thenReturn(false)
                .thenReturn(true);

        // When
        commentService.likeComment(10L);

        // Then
        verify(commentLikeRepository).save(any(CommentLike.class));
        verify(commentRepository).updateCommentLikesCount(10L, 1);
    }

    @Test
    public void shouldRemoveLikeAndDecreaseCount() {
        // Given
        AppUser user = new AppUser();
        user.setId(1L);
        when(userService.getLoggedUser()).thenReturn(user);

        Comment c = new Comment();
        c.setId(12L);
        when(commentRepository.findCommentById(12L)).thenReturn(c);

        CommentLike existing = new CommentLike(user, c, true);
        when(commentLikeRepository.findByCommentAndAppUser(c, user)).thenReturn(existing);

        when(commentLikeRepository.existsCommentLikeByAppUserAndComment(eq(user), eq(c)))
                .thenReturn(true)
                .thenReturn(false);

        // When
        commentService.likeComment(12L);

        // Then
        verify(commentLikeRepository).delete(existing);
        verify(commentRepository).updateCommentLikesCount(12L, -1);
    }

    @Test
    public void shouldThrowWhenLikeCommentNotFound() {
        // Given
        when(commentRepository.findCommentById(999L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.likeComment(999L));
        assertEquals(ErrorMessages.COMMENT_NOT_EXISTS, ex.getMessage());

        verifyNoInteractions(commentLikeRepository);
    }

    @Test
    public void shouldRemoveCommentWhenOwnerAndAdminAndDecreaseArticleCounter() {
        // Given
        AppUser admin = new AppUser();
        admin.setId(3L);
        admin.setUserRole(UserRole.ADMIN);

        Article article = new Article();
        article.setId(50L);

        Comment c = new Comment();
        c.setId(21L);
        c.setAppUser(admin);
        c.setArticle(article);

        when(commentRepository.findCommentById(21L)).thenReturn(c);
        when(userService.getLoggedUser()).thenReturn(admin);

        when(commentRepository.findCommentById(21L)).thenReturn(c).thenReturn(null);

        // When
        String res = commentService.removeComment(21L);

        // Then
        assertEquals("Removed", res);
        verify(commentRepository).deleteById(21L);
        verify(articleRepository).updateArticleLikesCount(50L, -1);
    }

    @Test
    public void shouldThrowWhenRemoveCommentNotFound() {
        // Given
        when(commentRepository.findCommentById(404L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.removeComment(404L));
        assertEquals(ErrorMessages.COMMENT_NOT_EXISTS, ex.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    public void shouldThrowWhenRemoveCommentWithoutPermission() {
        // Given
        AppUser user = new AppUser();
        user.setId(10L);
        user.setUserRole(UserRole.USER);

        AppUser owner = new AppUser();
        owner.setId(9L);

        Article article = new Article();
        article.setId(70L);

        Comment c = new Comment();
        c.setId(30L);
        c.setAppUser(owner);
        c.setArticle(article);

        when(commentRepository.findCommentById(30L)).thenReturn(c);
        when(userService.getLoggedUser()).thenReturn(user);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.removeComment(30L));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());

        verify(commentRepository, never()).deleteById(anyLong());
        verify(articleRepository, never()).updateArticleLikesCount(anyLong(), anyInt());
    }

    @Test
    public void shouldThrowWhenUpdateEmptyContent() {
        // Given
        CommentRequest req = new CommentRequest();
        req.setId(1L);
        req.setContent("");

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.updateComment(req));
        assertEquals(ErrorMessages.COMMENT_CANT_BE_EMPTY, ex.getMessage());
    }

    @Test
    public void shouldThrowWhenUpdateCommentNotFound() {
        // Given
        CommentRequest req = new CommentRequest();
        req.setId(2L);
        req.setContent("abc");

        when(commentRepository.findCommentById(2L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.updateComment(req));
        assertEquals(ErrorMessages.COMMENT_ID_NOT_FOUND + "2", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenUpdateCommentNotOwnedByUser() {
        // Given
        CommentRequest req = new CommentRequest();
        req.setId(3L);
        req.setContent("abc");

        AppUser owner = new AppUser();
        owner.setId(7L);
        AppUser other = new AppUser();
        other.setId(8L);

        Comment c = new Comment();
        c.setId(3L);
        c.setAppUser(owner);

        when(commentRepository.findCommentById(3L)).thenReturn(c);
        when(userService.getLoggedUser()).thenReturn(other);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> commentService.updateComment(req));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());
        verify(commentRepository, never()).updateCommentContent(anyLong(), anyString(), any());
    }

    @Test
    public void shouldUpdateCommentContentWhenValidAndOwned() {
        // Given
        CommentRequest req = new CommentRequest();
        req.setId(4L);
        req.setContent("new content");

        AppUser owner = new AppUser();
        owner.setId(7L);
        Comment c = new Comment();
        c.setId(4L);
        c.setAppUser(owner);

        when(commentRepository.findCommentById(4L)).thenReturn(c);
        when(userService.getLoggedUser()).thenReturn(owner);

        // When
        commentService.updateComment(req);

        // Then
        verify(commentRepository).updateCommentContent(eq(4L), eq("new content"), any());
    }

    @Test
    public void shouldPinComment() {
        // When
        commentService.pinComment(55L);
        // Then
        verify(commentRepository).pinComment(55L);
    }

    @Test
    public void shouldGetCommentsForUserWithPaginationAndLikedFlag() {
        // Given
        AppUser user = new AppUser();
        user.setId(33L);
        when(userService.getUserById(33L)).thenReturn(user);

        Comment c1 = new Comment();
        c1.setId(1L);
        Comment c2 = new Comment();
        c2.setId(2L);

        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.desc("postedDate")));
        Page<Comment> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        try (MockedStatic<GenericService> mocked = mockStatic(GenericService.class)) {
            mocked.when(() -> GenericService.paginate(eq(1), eq(3), eq("postedDate"), eq("DESC"), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Function<Pageable, Page<Comment>> supplier =
                                inv.getArgument(4);
                        when(commentRepository.getCommentsByAppUser(eq(user), any(Pageable.class)))
                                .thenReturn(page);
                        return supplier.apply(pageable);
                    });

            when(commentLikeRepository.findLikedCommentIdsByUserAndCommentIds(eq(user), anyList()))
                    .thenReturn(Set.of(2L));

            CommentDto d1 = new CommentDto();
            CommentDto d2 = new CommentDto();
            when(commentDtoMapper.toCommentDto(c1)).thenReturn(d1);
            when(commentDtoMapper.toCommentDto(c2)).thenReturn(d2);

            // When
            PageResponse<CommentDto> out = commentService.getCommentsForUser(33L, 1, 3);

            // Then
            assertEquals(2, out.getItems().size());
            assertFalse(out.getItems().get(0).isLiked());
            assertTrue(out.getItems().get(1).isLiked());
            assertEquals(2, out.getMeta().getTotalItems());
            assertEquals(1, out.getMeta().getTotalPages());
            assertEquals(1, out.getMeta().getCurrentPage());
            assertEquals(3, out.getMeta().getPageSize());
        }
    }

    @Test
    public void shouldReturnCommentsCountForUser() {
        // Given
        AppUser u = new AppUser();
        u.setId(9L);
        Comment c1 = new Comment();
        Comment c2 = new Comment();

        when(commentRepository.findAllByAppUser(u)).thenReturn(List.of(c1, c2));

        // When
        int count = commentService.getCommentsCountForUser(u);

        // Then
        assertEquals(2, count);
    }

    @Test
    void shouldThrowWhenCommentNotFound() {
        when(commentRepository.findCommentById(5L)).thenReturn(null);

        ResponseException ex = assertThrows(ResponseException.class,
                () -> commentService.likeComment(5L));

        assertEquals(ErrorMessages.COMMENT_NOT_EXISTS, ex.getMessage());
        verifyNoInteractions(commentLikeRepository);
    }

    @Test
    void shouldLikeWhenNotYetLiked_thenSaveAndIncrement() {
        // Given
        AppUser u = user(1L);
        Comment c = comment(10L);

        when(userService.getLoggedUser()).thenReturn(u);
        when(commentRepository.findCommentById(10L)).thenReturn(c);

        when(commentLikeRepository.existsCommentLikeByAppUserAndComment(u, c))
                .thenReturn(false, true);

        // When
        commentService.likeComment(10L);

        // Then
        verify(commentLikeRepository).save(argThat(cl ->
                cl.getAppUser().equals(u) &&
                        cl.getComment().equals(c) &&
                        Boolean.TRUE.equals(cl.isLiked())
        ));

        verify(commentRepository).updateCommentLikesCount(10L, 1);
    }

    @Test
    void shouldUnlikeWhenAlreadyLiked_thenDeleteAndDecrement() {
        // Given
        AppUser u = user(2L);
        Comment c = comment(20L);

        when(userService.getLoggedUser()).thenReturn(u);
        when(commentRepository.findCommentById(20L)).thenReturn(c);

        when(commentLikeRepository.existsCommentLikeByAppUserAndComment(u, c))
                .thenReturn(true, false);

        CommentLike existing = new CommentLike(u, c, true);
        when(commentLikeRepository.findByCommentAndAppUser(c, u)).thenReturn(existing);

        // When
        commentService.likeComment(20L);

        // Then
        verify(commentLikeRepository).delete(existing);
        verify(commentRepository).updateCommentLikesCount(20L, -1);
    }

    @Test
    void shouldNotIncrementIfExistsStillFalseAfterSave() {
        // Given
        AppUser u = user(3L);
        Comment c = comment(30L);

        when(userService.getLoggedUser()).thenReturn(u);
        when(commentRepository.findCommentById(30L)).thenReturn(c);

        when(commentLikeRepository.existsCommentLikeByAppUserAndComment(u, c))
                .thenReturn(false, false);

        // When
        commentService.likeComment(30L);

        // Then
        verify(commentLikeRepository).save(any(CommentLike.class));
        verify(commentRepository, never()).updateCommentLikesCount(anyLong(), eq(1));
    }

    @Test
    void removeCommentSuccessDeletesAndDecrements() {
        AppUser logged = new AppUser(); logged.setId(1L); logged.setUserRole(UserRole.ADMIN);
        when(userService.getLoggedUser()).thenReturn(logged);

        Comment c = new Comment(); c.setId(5L);
        Article a = new Article(); a.setId(100L); c.setArticle(a);
        AppUser owner = new AppUser(); owner.setId(1L); c.setAppUser(owner);

        when(commentRepository.findCommentById(5L)).thenReturn(c, (Comment) null);

        String res = commentService.removeComment(5L);

        assertEquals("Removed", res);
        verify(commentRepository).deleteById(5L);
        verify(articleRepository).updateArticleLikesCount(100L, -1);
    }
}
