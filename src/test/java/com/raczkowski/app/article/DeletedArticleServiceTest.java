package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeletedArticleServiceTest {

    @Mock
    private DeletedArticleRepository deletedArticleRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private DeletedArticleService deletedArticleService;

    @Test
    public void shouldDeleteExistingArticleAndSaveDeletedCopy() {
        // Given
        Long articleId = 100L;
        ArticleStatus status = ArticleStatus.DELETED_BY_ADMIN;

        AppUser author = new AppUser();
        author.setId(5L);

        AppUser admin = new AppUser();
        admin.setId(99L);

        AppUser moderator = new AppUser();
        moderator.setId(10L);

        Article article = new Article();
        article.setId(articleId);
        article.setTitle("T");
        article.setContent("C");
        article.setContentHtml("<p>C</p>");
        ZonedDateTime posted = ZonedDateTime.now(ZoneOffset.UTC).minusDays(10);
        article.setPostedDate(posted);
        article.setAppUser(author);
        article.setLikesCount(7);
        ZonedDateTime updatedAt = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
        article.setUpdatedAt(updatedAt);
        article.setUpdated(true);
        ZonedDateTime acceptedAt = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        article.setAcceptedAt(acceptedAt);
        article.setAcceptedBy(moderator);
        article.setCommentsCount(3);

        when(articleRepository.findArticleById(articleId)).thenReturn(article);

        ArgumentCaptor<DeletedArticle> captor = ArgumentCaptor.forClass(DeletedArticle.class);

        // When
        deletedArticleService.deleteArticle(articleId, status, admin);

        // Then
        verify(articleRepository).deleteArticleById(articleId);
        verify(deletedArticleRepository).save(captor.capture());

        DeletedArticle saved = captor.getValue();
        assertEquals("T", saved.getTitle());
        assertEquals("C", saved.getContent());
        assertEquals("<p>C</p>", saved.getContentHtml());
        assertEquals(posted, saved.getPostedDate());
        assertSame(author, saved.getAppUser());
        assertEquals(status, saved.getStatus());
        assertEquals(7, saved.getLikesNumber());
        assertEquals(updatedAt, saved.getUpdatedAt());
        assertTrue(saved.isUpdated());
        assertEquals(acceptedAt, saved.getAcceptedAt());
        assertSame(moderator, saved.getAcceptedBy());
        assertNotNull(saved.getDeletedAt());
        assertSame(admin, saved.getDeletedBy());
        assertEquals(3, saved.getCommentsCount());
    }

    @Test
    public void shouldThrowWhenArticleNotFound() {
        // Given
        Long articleId = 200L;
        when(articleRepository.findArticleById(articleId)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> deletedArticleService.deleteArticle(articleId, ArticleStatus.DELETED_BY_ADMIN, new AppUser()));
        assertEquals(ErrorMessages.ARTICLE_ID_NOT_EXISTS, ex.getMessage());

        verify(articleRepository, never()).deleteArticleById(anyLong());
        verify(deletedArticleRepository, never()).save(any(DeletedArticle.class));
    }
}