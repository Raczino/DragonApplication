package com.raczkowski.app.reddit;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedditPostServiceTest {

    @Mock
    private CommentService commentService;
    @Mock
    private ArticleService articleService;
    @Mock
    private RedditClient redditClient;
    @Mock
    private UserService userService;

    @InjectMocks
    private RedditPostService redditPostService;

    @Test
    public void shouldCreateCommentsFromRedditPostsForAllHashtags() throws IOException {
        // Given
        AppUser redditUser = new AppUser();
        redditUser.setId(22L);
        when(userService.getUserById(22L)).thenReturn(redditUser);

        Article a1 = new Article();
        a1.setHashtags(List.of(new Hashtag("java"), new Hashtag("spring")));
        Article a2 = new Article();
        a2.setHashtags(List.of(new Hashtag("testing")));
        when(articleService.getAllArticles()).thenReturn(List.of(a1, a2));

        RedditPost p1 = new RedditPost(
                null, "t1", 101, "url1", "Desc1", "auth1",
                ZonedDateTime.now(ZoneOffset.UTC).minusDays(1), "java"
        );
        RedditPost p2 = new RedditPost(
                null, "t2", 55, "url2", "Desc2", "auth2",
                ZonedDateTime.now(ZoneOffset.UTC).minusHours(2), "java"
        );
        RedditPost p3 = new RedditPost(
                null, "t3", 7, "url3", "Desc3", "auth3",
                ZonedDateTime.now(ZoneOffset.UTC), "testing"
        );

        when(redditClient.searchPostsOnSubreddit("java")).thenReturn(List.of(p1, p2));
        when(redditClient.searchPostsOnSubreddit("spring")).thenReturn(List.of()); // brak postów
        when(redditClient.searchPostsOnSubreddit("testing")).thenReturn(List.of(p3));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        // When
        redditPostService.getCommentsForArticle();

        // Then
        verify(commentService, times(3)).createComment(captor.capture());
        List<Comment> created = captor.getAllValues();

        assertSame(a1, created.get(0).getArticle());
        assertSame(a1, created.get(1).getArticle());
        assertSame(a2, created.get(2).getArticle());

        assertEquals("Desc1", created.get(0).getContent());
        assertEquals("url1", created.get(0).getRedditUrl());
        assertEquals("auth1", created.get(0).getRedditUsername());
        assertEquals(redditUser, created.get(0).getAppUser());
    }

    @Test
    public void shouldDoNothingWhenNoArticles() throws IOException {
        // Given
        when(articleService.getAllArticles()).thenReturn(List.of());

        AppUser redditUser = new AppUser();
        redditUser.setId(22L);
        when(userService.getUserById(22L)).thenReturn(redditUser);

        // When
        redditPostService.getCommentsForArticle();

        // Then
        verifyNoInteractions(redditClient);
        verifyNoMoreInteractions(commentService);
        verify(userService).getUserById(22L);
    }

    @Test
    public void shouldSkipWhenNoHashtagsOrNoPosts() throws IOException {
        // Given
        AppUser redditUser = new AppUser();
        redditUser.setId(22L);
        when(userService.getUserById(22L)).thenReturn(redditUser);

        Article withNoTags = new Article();
        withNoTags.setHashtags(List.of());
        Article withTagButNoPosts = new Article();
        withTagButNoPosts.setHashtags(List.of(new Hashtag("empty")));

        when(articleService.getAllArticles()).thenReturn(List.of(withNoTags, withTagButNoPosts));
        when(redditClient.searchPostsOnSubreddit("empty")).thenReturn(List.of());

        // When
        redditPostService.getCommentsForArticle();

        // Then
        verify(commentService, never()).createComment(any());
    }

    @Test
    public void shouldPropagateIOExceptionAndNotCreateComments() throws IOException {
        // Given
        AppUser redditUser = new AppUser();
        redditUser.setId(22L);
        when(userService.getUserById(22L)).thenReturn(redditUser);

        Article a = new Article();
        a.setHashtags(List.of(new Hashtag("fail")));
        when(articleService.getAllArticles()).thenReturn(List.of(a));

        when(redditClient.searchPostsOnSubreddit("fail"))
                .thenThrow(new IOException("reddit down"));

        // When & Then
        IOException ex = assertThrows(IOException.class,
                () -> redditPostService.getCommentsForArticle());
        assertEquals("reddit down", ex.getMessage());
        verify(commentService, never()).createComment(any());
    }
}
