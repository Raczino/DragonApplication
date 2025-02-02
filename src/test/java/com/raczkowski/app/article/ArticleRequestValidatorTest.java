package com.raczkowski.app.article;

import com.raczkowski.app.Reddit.RedditClientConfig;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleRequestValidatorTest {

    @Mock
    private UserService userService;
    @Mock
    private ArticleRequestValidator articleRequestValidator;
    @Mock
    private ArticleRepository articleRepository;

    @Test
    void validateCreationRequest() {
    }

    @Test
    void validateUpdateRequest() {
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnExceptionForNullValueOfTitle() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest(null, "content");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        doThrow(new ResponseException("Title or content can't be empty"))
                .when(articleRequestValidator)
                .validateArticleRequest(any());

        // when
        ResponseException exception = assertThrows(ResponseException.class, () -> articleRequestValidator.validateArticleRequest(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }

    @Test
    public void shouldReturnExceptionForNullValueOfContent() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("title", null);
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        doThrow(new ResponseException("Title or content can't be empty"))
                .when(articleRequestValidator)
                .validateArticleRequest(any());

        // when
        ResponseException exception = assertThrows(ResponseException.class, () -> articleRequestValidator.validateArticleRequest(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }

    @Test
    public void shouldReturnExceptionForEmptyTitle() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("", "title");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        doThrow(new ResponseException("Title or content can't be empty"))
                .when(articleRequestValidator)
                .validateArticleRequest(any());
        // when
        ResponseException exception = assertThrows(ResponseException.class, () -> articleRequestValidator.validateArticleRequest(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }

    @Test
    public void shouldReturnExceptionForEmptyContent() {
        // given
        ArticleRequest emptyRequest = new ArticleRequest("title", "");
        when(userService.getLoggedUser()).thenReturn(new AppUser());

        doThrow(new ResponseException("Title or content can't be empty"))
                .when(articleRequestValidator)
                .validateArticleRequest(any());
        // when
        ResponseException exception = assertThrows(ResponseException.class, () -> articleRequestValidator.validateArticleRequest(emptyRequest));

        // then
        assertEquals("Title or content can't be empty", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }
}