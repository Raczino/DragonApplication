package com.raczkowski.app.article;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ModerationArticleService;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private ModerationArticleService moderationArticleService;
    @Mock
    private ArticleLikeRepository articleLikeRepository;
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
        assertEquals(createdArticle.getStatus(), ArticleStatus.PENDING);
        verify(moderationArticleService, times(1)).addArticle(any());
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
    public void ShouldGetArticlesFromUser() {
        // given
        Long userId = 1L;
        AppUser user = new AppUser();
        user.setId(userId);
        when(userRepository.getAppUserById(userId)).thenReturn(user);

        ZonedDateTime postedDate = ZonedDateTime.now();
        List<Article> articles = new ArrayList<>();
        articles.add(new Article("Title1", "Content1", postedDate, user));
        articles.add(new Article("Title2", "Content2", postedDate, user));
        when(articleRepository.findAllByAppUser(user)).thenReturn(articles);

        // when
        List<Article> result = articleService.getArticlesFromUser(userId);

        // then
        assertEquals(2, result.size());
        assertEquals("Title1", result.get(0).getTitle());
        assertEquals("Content1", result.get(0).getContent());
        assertEquals(postedDate, result.get(0).getPostedDate());
        assertEquals(user.getId(), result.get(0).getAppUser().getId());
        assertEquals("Title2", result.get(1).getTitle());
        assertEquals("Content2", result.get(1).getContent());
        assertEquals(postedDate, result.get(1).getPostedDate());
        assertEquals(user.getId(), result.get(1).getAppUser().getId());
        verify(userRepository, times(1)).getAppUserById(userId);
        verify(articleRepository, times(1)).findAllByAppUser(user);
    }

    @Test
    public void ShouldReturnEmptyListForUserWithoutPostedArticle() {
        // given
        Long userId = 123L;
        when(userRepository.getAppUserById(userId)).thenReturn(new AppUser());

        // when
        List<Article> result = articleService.getArticlesFromUser(userId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
}

