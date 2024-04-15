package com.raczkowski.app.article;

import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private ArticleLikeRepository articleLikeRepository;

    @InjectMocks
    private ArticleService articleService;

    private final AppUser user = new AppUser("username", "password", "test@test.pl");

    public ArticleServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnSavedForCorrectArticleSave() {
        // given
        ArticleRequest request = new ArticleRequest("Title", "Content");
        AppUser mockUser = new AppUser("firstName", "username", "password");
        when(userService.getLoggedUser()).thenReturn(mockUser);

        // when
        ArticleDto createdArticle = articleService.create(request);

        // then
        assertEquals(createdArticle.getTitle(), request.getTitle());
        assertEquals(createdArticle.getContent(), request.getContent());
        assertEquals(createdArticle.getUser().getFirstName(), mockUser.getFirstName());
        assertEquals(createdArticle.getUser().getLastName(), mockUser.getLastName());
        assertEquals(createdArticle.getUser().getEmail(), mockUser.getEmail());
        verify(articleRepository, times(1)).save(any());
        verify(userRepository, times(1)).updateArticlesCount(any());
    }

    @Test
    public void shouldReturnExceptionForNullValueOfTitle() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest(null, "content");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
        verify(userRepository, never()).updateArticlesCount(any());
    }

    @Test
    public void shouldReturnExceptionForNullValueOfContent() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("title", null);
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
        verify(userRepository, never()).updateArticlesCount(any());
    }

    @Test
    public void shouldReturnExceptionForEmptyTitle() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("", "title");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
        verify(userRepository, never()).updateArticlesCount(any());
    }

    @Test
    public void shouldReturnExceptionForEmptyContent() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("title", "");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        // when
        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
        verify(userRepository, never()).updateArticlesCount(any());
    }

    @Test
    public void ShouldGetArticlesFromUser() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ZonedDateTime postedDate = ZonedDateTime.now();
        List<Article> articles = new ArrayList<>();
        articles.add(new Article("Title1", "Content1", postedDate, user));
        articles.add(new Article("Title2", "Content2", postedDate, user));
        when(articleRepository.findAllByAppUser(Optional.of(user))).thenReturn(articles);

        // when
        List<ArticleDto> result = articleService.getArticlesFromUser(userId);

        // then
        assertEquals(2, result.size());
        assertEquals("Title1", result.get(0).getTitle());
        assertEquals("Content1", result.get(0).getContent());
        assertEquals(postedDate, result.get(0).getPostedDate());
        assertEquals(user.getId(), result.get(0).getUser().getId());
        assertEquals("Title2", result.get(1).getTitle());
        assertEquals("Content2", result.get(1).getContent());
        assertEquals(postedDate, result.get(1).getPostedDate());
        assertEquals(user.getId(), result.get(1).getUser().getId());
        verify(userRepository, times(1)).findById(userId);
        verify(articleRepository, times(1)).findAllByAppUser(Optional.of(user));
    }

    @Test
    public void ShouldReturnEmptyListForUserWithoutPostedArticle() {
        // given
        Long userId = 123L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        List<ArticleDto> result = articleService.getArticlesFromUser(userId);

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
        verify(articleRepository, times(1)).updateArticleLikes(articleId, 1);
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
        verify(articleRepository, times(1)).updateArticleLikes(articleId, -1);
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
        verify(articleRepository, never()).updateArticleLikes(any(), anyInt());
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
        ArticleDto result = articleService.getArticleByID(articleId);

        // then
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals(userId, result.getUser().getId());
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
}

