package com.raczkowski.app.article;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.admin.moderation.article.ModerationArticleService;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
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
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ArticleRequestValidator articleRequestValidator;
    @Mock
    private ModerationArticleService moderationArticleService;
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
        assertEquals("Article doesnt exists", exception.getMessage());
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
        assertEquals("There is no article with provided id", exception.getMessage());
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

        assertEquals("User doesn't have permission to update this comment", exception.getMessage());
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

}

