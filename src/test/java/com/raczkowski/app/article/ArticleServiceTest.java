package com.raczkowski.app.article;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.hashtags.HashtagService;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserService userService;
    @Mock
    private ArticleRequestValidator articleRequestValidator;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private ArticleLikeRepository articleLikeRepository;
    @Mock
    private ArticleToConfirmRepository articleToConfirmRepository;
    @Mock
    FeatureLimitHelperService featureLimitHelperService;
    @Mock
    private DeletedArticleService deletedArticleService;
    @Mock
    private ArticleDtoMapper articleDtoMapper;
    @InjectMocks
    private ArticleService articleService;
    private final AppUser user = new AppUser("username", "password", "test@test.pl");

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnSavedForCorrectArticleSave() {
        // given
        ArticleRequest request = new ArticleRequest("Title", "Content");
        AppUser mockUser = new AppUser("firstName", "lastName", "email");
        when(userService.getLoggedUser()).thenReturn(mockUser);

        // when
        ArticleToConfirm createdArticle = articleService.create(request);

        // then
        assertEquals(createdArticle.getTitle(), request.getTitle());
        assertEquals(createdArticle.getContent(), request.getContent());
        assertEquals(createdArticle.getAppUser().getFirstName(), mockUser.getFirstName());
        assertEquals(createdArticle.getAppUser().getLastName(), mockUser.getLastName());
        assertEquals(createdArticle.getAppUser().getEmail(), mockUser.getEmail());
        assertEquals(ArticleStatus.PENDING, createdArticle.getStatus());
        verify(articleToConfirmRepository, times(1)).save(any());
    }

    @Test
    public void ShouldReturnAllArticles() {
        //given:
        List<Article> articlesList = new ArrayList<>();
        articlesList.add(new Article());
        articlesList.add(new Article());
        when(articleRepository.findAll()).thenReturn(articlesList);

        //when:
        List<Article> allArticles = articleService.getAllArticles();

        //then:
        assertEquals(2, allArticles.size());
    }

    @Test
    public void shouldReturnArticlesFromUser() {
        // given
        Long userId = 1L;
        AppUser user = new AppUser();
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        ZonedDateTime postedDate = ZonedDateTime.now();
        Article article1 = new Article("Title1", "Content1", postedDate, user);
        Article article2 = new Article("Title2", "Content2", postedDate, user);

        article1.setStatus(ArticleStatus.APPROVED);
        article2.setStatus(ArticleStatus.APPROVED);

        article1.setId(101L);
        article2.setId(102L);

        List<Article> articles = Arrays.asList(article1, article2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "likesNumber"));
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.getArticleByAcceptedBy(eq(user), any(Pageable.class))).thenReturn(articlePage);

        when(articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(eq(user), anyList())).thenReturn(Collections.emptySet());

        when(articleDtoMapper.toArticleDto(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            ArticleDto dto = new ArticleDto();
            dto.setTitle(article.getTitle());
            dto.setContent(article.getContent());
            return dto;
        });

        int pageNumber = 1;
        int pageSize = 10;
        String sortBy = "likesNumber";
        String sortDirection = "asc";

        // when
        PageResponse<ArticleDto> result = articleService.getArticlesFromUser(userId, pageNumber, pageSize, sortBy, sortDirection);

        // then
        assertNotNull(result);
        assertEquals(2, result.getItems().size());

        ArticleDto dto1 = result.getItems().get(0);
        assertEquals("Title1", dto1.getTitle());
        assertEquals("Content1", dto1.getContent());

        ArticleDto dto2 = result.getItems().get(1);
        assertEquals("Title2", dto2.getTitle());
        assertEquals("Content2", dto2.getContent());

        verify(userService, times(1)).getUserById(userId);
        verify(articleRepository, times(1)).getArticleByAcceptedBy(eq(user), any(Pageable.class));
        verify(articleLikeRepository, times(1)).findLikedArticleIdsByUserAndArticleIds(eq(user), anyList());
        verify(articleDtoMapper, times(2)).toArticleDto(any(Article.class));
    }

    @Test
    public void shouldReturnEmptyListWhenUserHasNoArticles() {
        // given
        Long userId = 123L;
        AppUser user = new AppUser();
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(articleRepository.getArticleByAcceptedBy(eq(user), any(Pageable.class))).thenReturn(emptyPage);

        // when
        PageResponse<ArticleDto> result = articleService.getArticlesFromUser(userId, 1, 10, "title", "asc");

        // then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());

        verify(userService, times(1)).getUserById(userId);
        verify(articleRepository, times(1)).getArticleByAcceptedBy(eq(user), any(Pageable.class));
    }


    @Test
    public void shouldLikeArticleSuccessfully() {
        // given
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);
        when(userService.getLoggedUser()).thenReturn(user);
        when(articleRepository.findArticleById(articleId)).thenReturn(article);
        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)).thenReturn(false);

        // when
        articleService.likeArticle(articleId);

        // then
        verify(articleLikeRepository, times(1)).save(any());
    }

    @Test
    public void shouldUnlikeArticleSuccessfully() {
        // given
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);
        when(userService.getLoggedUser()).thenReturn(user);
        when(articleRepository.findArticleById(articleId)).thenReturn(article);
        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)).thenReturn(true);

        // when
        articleService.likeArticle(articleId);

        // then
        verify(articleLikeRepository, times(1)).delete(any());
    }

    @Test
    public void shouldThrowExceptionIfArticleDoesNotExist() {
        // given
        Long articleId = 1L;
        when(articleRepository.findArticleById(articleId)).thenReturn(null);

        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.likeArticle(articleId));

        // then
        assertEquals(ErrorMessages.ARTICLE_ID_NOT_EXISTS, exception.getMessage());
        assertThrows(Exception.class, () -> articleService.likeArticle(articleId));
        verify(articleLikeRepository, never()).save(any());
    }

    @Test
    public void shouldGetArticleByIDSuccessfully() {
        // given
        Long articleId = 1L;
        Long userId = 1L;
        Article article = new Article();
        article.setId(articleId);
        article.setAppUser(user);
        user.setId(userId);
        when(articleRepository.findArticleById(articleId)).thenReturn(article);

        // when
        Article result = articleService.getArticleByID(articleId);

        // then
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals(userId, result.getAppUser().getId());
        verify(articleRepository, times(1)).findArticleById(articleId);
    }

    @Test
    public void shouldThrowExceptionWhenArticleNotFound() {
        // given
        Long articleId = 1L;
        when(articleRepository.findArticleById(articleId)).thenReturn(null);
        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.getArticleByID(articleId));
        // then
        assertEquals(ErrorMessages.ARTICLE_ID_NOT_EXISTS, exception.getMessage());
        verify(articleRepository, times(1)).findArticleById(articleId);
    }

    @Test
    public void shouldReturnFalseWhenArticleNotLikedByUser() {
        // given
        Article article = new Article();
        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)).thenReturn(false);

        // when
        boolean result = articleService.isArticleLiked(article, user);

        // then
        assertFalse(result);
        verify(articleLikeRepository, times(1)).existsArticleLikesByAppUserAndArticle(user, article);
    }

    @Test
    public void shouldReturnTrueWhenArticleLikedByUser() {
        // given
        Article article = new Article();
        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)).thenReturn(true);

        // when
        boolean result = articleService.isArticleLiked(article, user);

        // then
        assertTrue(result);
        verify(articleLikeRepository, times(1)).existsArticleLikesByAppUserAndArticle(user, article);
    }

    @Test
    void shouldUpdateTitleAndContentWhenBothProvided() {
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setId(1L);
        articleRequest.setTitle("New Title");
        articleRequest.setContent("New Content");

        AppUser user = new AppUser();
        user.setId(1L);

        Article article = new Article();
        article.setAppUser(user);
        article.getAppUser().setId(1L);

        when(articleRepository.findArticleById(1L)).thenReturn(article);
        when(userService.getLoggedUser()).thenReturn(user);

        doNothing().when(articleRequestValidator).validateArticleRequest(articleRequest, user);
        articleService.updateArticle(articleRequest);

        verify(articleRepository).updateArticle(
                eq(1L),
                eq("New Title"),
                eq("New Content"),
                any(ZonedDateTime.class)
        );
    }

    @Test
    void shouldUpdateTitleWhenTitleProvided() {
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setId(1L);
        articleRequest.setTitle("New Title");

        AppUser user = new AppUser();
        user.setId(1L);

        Article article = new Article();
        article.setAppUser(user);
        article.getAppUser().setId(1L);
        article.setContent("Old Content");


        when(articleRepository.findArticleById(1L)).thenReturn(article);
        when(userService.getLoggedUser()).thenReturn(user);

        doNothing().when(articleRequestValidator).validateArticleRequest(articleRequest, user);

        articleService.updateArticle(articleRequest);

        verify(articleRepository).updateArticle(
                eq(1L),
                eq("New Title"),
                eq("Old Content"),
                any(ZonedDateTime.class)
        );
    }

    @Test
    void shouldUpdateContentWhenContentProvided() {
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setId(1L);
        articleRequest.setContent("New Content");

        AppUser user = new AppUser();
        user.setId(1L);

        Article article = new Article();
        article.setAppUser(user);
        article.getAppUser().setId(1L);
        article.setTitle("Old Title");

        when(articleRepository.findArticleById(1L)).thenReturn(article);
        when(userService.getLoggedUser()).thenReturn(user);

        doNothing().when(articleRequestValidator).validateArticleRequest(articleRequest, user);

        articleService.updateArticle(articleRequest);

        verify(articleRepository).updateArticle(
                eq(1L),
                eq("Old Title"),
                eq("New Content"),
                any(ZonedDateTime.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdateArticleAndArticleNotFound() {
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setId(1L);

        when(articleRepository.findArticleById(1L)).thenReturn(null);

        assertThrows(ResponseException.class, () -> articleService.updateArticle(articleRequest));
        verify(articleRepository, never()).updateArticle(anyLong(), anyString(), anyString(), any());
    }

    @Test
    void shouldPinArticle() {
        Long articleId = 1L;
        AppUser user = new AppUser();
        user.setId(10L);

        articleService.pinArticle(articleId, user);

        verify(articleRepository, times(1)).pinArticle(articleId, user);
    }

    @Test
    void shouldReturnArticlesCountForUser() {
        AppUser user = new AppUser();
        List<Article> articles = List.of(new Article(), new Article());

        when(articleRepository.findAllByAppUser(user)).thenReturn(articles);

        int count = articleService.getArticlesCountForUser(user);

        assertEquals(2, count);
    }

    @Test
    void shouldRemoveArticle() {
        Long articleId = 42L;

        articleService.removeArticle(articleId);

        verify(deletedArticleService).deleteArticle(articleId, ArticleStatus.DELETED, null);
    }

    @Test
    void shouldThrowExceptionWhenUserNotOwnerWhileUpdating() {
        ArticleRequest request = new ArticleRequest();
        request.setId(1L);
        request.setTitle("Title");
        request.setContent("Content");

        AppUser user = new AppUser();
        user.setId(1L);

        AppUser anotherUser = new AppUser();
        anotherUser.setId(2L);

        Article article = new Article();
        article.setAppUser(anotherUser);

        when(articleRepository.findArticleById(1L)).thenReturn(article);
        when(userService.getLoggedUser()).thenReturn(user);
        doNothing().when(articleRequestValidator).validateArticleRequest(request, user);

        ResponseException exception = assertThrows(ResponseException.class, () -> articleService.updateArticle(request));

        assertEquals(ErrorMessages.WRONG_PERMISSION, exception.getMessage());
    }

    @Test
    void shouldPublishScheduledArticles() {
        Article article1 = new Article();
        article1.setId(1L);
        article1.setScheduledForDate(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        article1.setStatus(ArticleStatus.SCHEDULED);

        Article article2 = new Article();
        article2.setId(2L);
        article2.setScheduledForDate(ZonedDateTime.now(ZoneOffset.UTC));
        article2.setStatus(ArticleStatus.SCHEDULED);

        List<Article> articles = List.of(article1, article2);

        when(articleRepository.getAllByStatus(ArticleStatus.SCHEDULED)).thenReturn(articles);

        articleService.publishArticle();

        verify(articleRepository, times(1)).updateArticleStatus(article1.getId());
        verify(articleRepository, times(1)).updateArticleStatus(article2.getId());
    }

    @Test
    void shouldPaginateAndReturnApprovedArticles() {
        AppUser user = new AppUser();
        user.setId(1L);

        Article article = new Article();
        article.setId(1L);
        article.setStatus(ArticleStatus.APPROVED);

        @SuppressWarnings("unchecked")
        Page<Article> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(article));
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);

        when(userService.getLoggedUser()).thenReturn(user);
        when(articleRepository.findAllWithPinnedFirst(any())).thenReturn(page);
        when(articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(eq(user), anyList())).thenReturn(Set.of(1L));
        when(articleDtoMapper.toArticleDto(any())).thenReturn(new ArticleDto());

        PageResponse<ArticleDto> result = articleService.getAllArticles(1, 10, "postedDate", "desc");

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getMeta().getTotalItems());
    }

    @Test
    void shouldGetArticlesFromUserPaginated() {
        Long userId = 1L;
        AppUser user = new AppUser();
        user.setId(userId);

        Article article = new Article();
        article.setId(1L);
        article.setStatus(ArticleStatus.APPROVED);

        @SuppressWarnings("unchecked")
        Page<Article> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(article));
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);

        when(userService.getUserById(userId)).thenReturn(user);
        when(articleRepository.getArticleByAcceptedBy(eq(user), any())).thenReturn(page);
        when(articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(eq(user), anyList())).thenReturn(Set.of(1L));
        when(articleDtoMapper.toArticleDto(any())).thenReturn(new ArticleDto());

        PageResponse<ArticleDto> result = articleService.getArticlesFromUser(userId, 1, 10, "postedDate", "desc");

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getMeta().getTotalItems());
    }

    @Test
    public void shouldCreateArticleWithHashtagsAndIncrementLimit() {
        // Given
        AppUser mockUser = new AppUser();
        mockUser.setId(77L);
        when(userService.getLoggedUser()).thenReturn(mockUser);

        ArticleRequest req = new ArticleRequest("T", "C");
        req.setContentHtml("<p>C</p>");
        req.setHashtags("#java #spring");

        List<Hashtag> parsed = List.of(new Hashtag("java"), new Hashtag("spring"));
        when(hashtagService.parseHashtags("#java #spring")).thenReturn(parsed);

        // When
        ArticleToConfirm out = articleService.create(req);

        // Then
        assertEquals("T", out.getTitle());
        assertEquals("C", out.getContent());
        assertEquals(mockUser, out.getAppUser());
        assertEquals(ArticleStatus.PENDING, out.getStatus());
        assertEquals(2, out.getHashtags().size());
        verify(articleToConfirmRepository).save(any(ArticleToConfirm.class));
        verify(featureLimitHelperService).incrementFeatureUsage(77L, FeatureKeys.ARTICLE_COUNT_PER_WEEK);
        verify(articleRequestValidator).validateArticleRequest(req, mockUser);
    }

    @Test
    public void shouldFilterOnlyApprovedInGetAllArticles() {
        AppUser u = new AppUser();
        u.setId(1L);
        when(userService.getLoggedUser()).thenReturn(u);

        Article a1 = new Article();
        a1.setId(1L);
        a1.setStatus(ArticleStatus.APPROVED);
        Article a2 = new Article();
        a2.setId(2L);
        a2.setStatus(ArticleStatus.PENDING);
        Article a3 = new Article();
        a3.setId(3L);
        a3.setStatus(ArticleStatus.SCHEDULED);

        @SuppressWarnings("unchecked")
        Page<Article> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(a1, a2, a3));
        when(page.getTotalElements()).thenReturn(3L);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);

        when(articleRepository.findAllWithPinnedFirst(any())).thenReturn(page);
        when(articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(eq(u), anyList())).thenReturn(Set.of());
        when(articleDtoMapper.toArticleDto(a1)).thenReturn(new ArticleDto());

        PageResponse<ArticleDto> res = articleService.getAllArticles(1, 10, "postedDate", "desc");

        assertEquals(1, res.getItems().size());
        assertEquals(3, res.getMeta().getTotalItems());
    }

    @Test
    public void shouldIncreaseLikesCounterAfterConfirmSavedLike() {
        Long id = 10L;
        AppUser u = new AppUser();
        Article a = new Article();
        a.setId(id);

        when(userService.getLoggedUser()).thenReturn(u);
        when(articleRepository.findArticleById(id)).thenReturn(a);

        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(eq(u), eq(a)))
                .thenReturn(false, true);

        // When
        articleService.likeArticle(id);

        // Then
        verify(articleLikeRepository).save(any(ArticleLike.class));
        verify(articleRepository).updateArticleLikesCount(id, 1);
        verify(articleLikeRepository, never()).delete(any());
        verify(articleLikeRepository, never()).findByArticleAndAppUser(any(), any());
    }

    @Test
    public void shouldDecreaseLikesCounterAfterConfirmUnlike() {
        Long id = 11L;
        AppUser u = new AppUser();
        Article a = new Article();
        a.setId(id);

        when(userService.getLoggedUser()).thenReturn(u);
        when(articleRepository.findArticleById(id)).thenReturn(a);

        when(articleLikeRepository.existsArticleLikesByAppUserAndArticle(eq(u), eq(a)))
                .thenReturn(true, false);

        ArticleLike existing = mock(ArticleLike.class);
        when(articleLikeRepository.findByArticleAndAppUser(a, u)).thenReturn(existing);

        // When
        articleService.likeArticle(id);

        // Then
        verify(articleLikeRepository).delete(existing);
        verify(articleRepository).updateArticleLikesCount(id, -1);
        verify(articleLikeRepository, never()).save(any());
    }

    @Test
    public void shouldNotPublishWhenScheduledAfterNow() {
        Article future = new Article();
        future.setId(1L);
        future.setStatus(ArticleStatus.SCHEDULED);
        future.setScheduledForDate(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(2));

        when(articleRepository.getAllByStatus(ArticleStatus.SCHEDULED))
                .thenReturn(List.of(future));

        articleService.publishArticle();

        verify(articleRepository, never()).updateArticleStatus(anyLong());
    }

    @Test
    public void shouldSetLikedFalseWhenUserHasNoLikes() {
        AppUser u = new AppUser();
        u.setId(1L);
        when(userService.getLoggedUser()).thenReturn(u);

        Article a = new Article();
        a.setId(5L);
        a.setStatus(ArticleStatus.APPROVED);

        @SuppressWarnings("unchecked")
        Page<Article> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(a));
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);

        when(articleRepository.findAllWithPinnedFirst(any())).thenReturn(page);
        when(articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(eq(u), anyList())).thenReturn(Set.of());
        ArticleDto dto = new ArticleDto();
        dto.setLiked(true);
        when(articleDtoMapper.toArticleDto(a)).thenReturn(dto);

        PageResponse<ArticleDto> res = articleService.getAllArticles(1, 10, "postedDate", "desc");

        assertEquals(1, res.getItems().size());
        assertFalse(res.getItems().get(0).isLiked());
    }

    @Test
    void shouldReturnArticlesAcceptedByUserFromRepository() {
        // given
        AppUser user = new AppUser();
        user.setId(1L);

        Pageable pageable = PageRequest.of(0, 5);
        Article article1 = new Article();
        article1.setId(100L);
        Article article2 = new Article();
        article2.setId(200L);

        Page<Article> expectedPage = new PageImpl<>(List.of(article1, article2), pageable, 2);

        when(articleRepository.getArticleByAcceptedBy(user, pageable)).thenReturn(expectedPage);

        // when
        Page<Article> result = articleService.getArticlesAcceptedByUser(user, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(100L, result.getContent().get(0).getId());
        assertEquals(200L, result.getContent().get(1).getId());

        verify(articleRepository, times(1)).getArticleByAcceptedBy(user, pageable);
    }

    @Test
    void shouldSaveArticleUsingRepository() {
        // given
        Article article = new Article();
        article.setId(10L);
        article.setTitle("Test title");

        // when
        articleService.saveArticle(article);

        // then
        verify(articleRepository, times(1)).save(article);
    }

}

