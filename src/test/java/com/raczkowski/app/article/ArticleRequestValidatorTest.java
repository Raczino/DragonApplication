package com.raczkowski.app.article;

import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleRequestValidatorTest {

    @Test
    void validateCreationRequest() {
    }

    @Test
    void validateUpdateRequest() {
    }

//    @Test
//    public void shouldReturnExceptionForNullValueOfTitle() {
//        // given
//        ArticleRequest emptyRequest = new ArticleRequest(null, "content");
//        when(userService.getLoggedUser()).thenReturn(new AppUser());
//
//        // when
//        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));
//
//        // then
//        assertEquals("Title or content can't be empty", exception.getMessage());
//        verify(articleRepository, never()).save(any());
//    }
//
//    @Test
//    public void shouldReturnExceptionForNullValueOfContent() {
//        // given
//        ArticleRequest emptyRequest = new ArticleRequest("title", null);
//        when(userService.getLoggedUser()).thenReturn(new AppUser());
//
//        // when
//        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));
//
//        // then
//        assertEquals("Title or content can't be empty", exception.getMessage());
//        verify(articleRepository, never()).save(any());
//    }
//
//    @Test
//    public void shouldReturnExceptionForEmptyTitle() {
//        // given
//        ArticleRequest emptyRequest = new ArticleRequest("", "title");
//        when(userService.getLoggedUser()).thenReturn(new AppUser());
//
//        // when
//        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));
//
//        // then
//        assertEquals("Title or content can't be empty", exception.getMessage());
//        verify(articleRepository, never()).save(any());
//    }
//
//    @Test
//    public void shouldReturnExceptionForEmptyContent() {
//        // given
//        ArticleRequest emptyRequest = new ArticleRequest("title", "");
//        when(userService.getLoggedUser()).thenReturn(new AppUser());
//
//        // when
//        Exception exception = assertThrows(Exception.class, () -> articleService.create(emptyRequest));
//
//        // then
//        assertEquals("Title or content can't be empty", exception.getMessage());
//        verify(articleRepository, never()).save(any());
//    }
}