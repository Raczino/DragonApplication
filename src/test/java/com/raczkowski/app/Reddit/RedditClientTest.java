package com.raczkowski.app.Reddit;

import com.raczkowski.app.http.HttpConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedditClientTest {

    @Mock
    private RedditPostRepository redditPostRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedditClientConfig redditClientConfig;

    @Mock
    private HttpConnectionFactory httpConnectionFactory;
    @InjectMocks
    RedditClient client;

    @Test
    void shouldDelegateExistenceCheckToRepoByUrl() {
        RedditPost p = new RedditPost();
        p.setUrl("https://u1");
        when(redditPostRepository.existsByUrl("https://u1")).thenReturn(true);

        boolean exists = client.checkIfPostAlreadyExists(p);

        assertTrue(exists);
        verify(redditPostRepository).existsByUrl("https://u1");
    }

    @Test
    void shouldOpenConnectionAndSetHeadersForGET() throws Exception {
        // given
        RedditClient client = new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionFactory.open("http://test")).thenReturn(conn);
        when(redditClientConfig.getUser().getAgent()).thenReturn("JUnit-Agent");

        // when
        HttpURLConnection result = client.sendHttpRequest("http://test", "GET", "Bearer abc", null);

        // then
        assertSame(conn, result);
        verify(conn).setRequestMethod("GET");
        verify(conn).setRequestProperty("Authorization", "Bearer abc");
        verify(conn).setRequestProperty("User-Agent", "JUnit-Agent");
        verify(conn).setRequestProperty("Accept", "application/json");
        verify(conn, never()).setDoOutput(true);
    }

    @Test
    void shouldReturnAccessTokenWhenResponseContainsToken() throws Exception {
        RedditClient client = Mockito.spy(new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory));

        when(redditClientConfig.getClient().getId()).thenReturn("cid");
        when(redditClientConfig.getClient().getSecret()).thenReturn("csec");

        HttpURLConnection conn = mock(HttpURLConnection.class);
        doReturn(conn).when(client).sendHttpRequest(anyString(), eq("POST"), anyString(), anyString());
        doReturn("{\"access_token\":\"ABC123\"}").when(client).readResponse(conn);

        String token = client.getAccessToken();

        assertEquals("ABC123", token);
        verify(client).sendHttpRequest(contains("/api/v1/access_token"), eq("POST"), contains("Basic "), eq("grant_type=client_credentials"));
        verify(client).readResponse(conn);
    }

    @Test
    void shouldThrowExceptionaWhenAccessTokenMissingInResponse() throws Exception {
        // Given
        RedditClient client = Mockito.spy(new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory));

        when(redditClientConfig.getClient().getId()).thenReturn("cid");
        when(redditClientConfig.getClient().getSecret()).thenReturn("csec");

        HttpURLConnection conn = mock(HttpURLConnection.class);
        doReturn(conn).when(client).sendHttpRequest(anyString(), eq("POST"), anyString(), anyString());
        doReturn("{}").when(client).readResponse(conn);

        // When & Then
        assertThrows(java.io.IOException.class, client::getAccessToken);
    }

    @Test
    void shouldSearchAndSaveOnlyNewPostsWithDescriptionAndReturnSavedList() throws Exception {
        // Given
        RedditClient client = Mockito.spy(new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory));

        when(redditClientConfig.getUser().getSubreddit()).thenReturn("java");

        doReturn("TOK").when(client).getAccessToken();

        HttpURLConnection conn = mock(HttpURLConnection.class);
        doReturn(conn).when(client).sendHttpRequest(contains("/search.json"), eq("GET"), anyString(), isNull());

        String json = """
                {
                  "data": {
                    "children": [
                      { "data": {
                          "title":"T1","url":"https://u1","selftext":"desc1","score":5,"author":"a1","created_utc":100
                      }},
                      { "data": {
                          "title":"T2","url":"https://u2","selftext":"", "score":3,"author":"a2","created_utc":101
                      }},
                      { "data": {
                          "title":"T3","url":"https://dup","selftext":"dupdesc","score":1,"author":"a3","created_utc":102
                      }},
                      { "data": {
                          "title":"T4","url":"https://u3","selftext":"desc3","score":10,"author":"a4"
                      }}
                    ]
                  }
                }""";
        doReturn(json).when(client).readResponse(conn);

        when(redditPostRepository.existsByUrl("https://u1")).thenReturn(false);
        when(redditPostRepository.existsByUrl("https://dup")).thenReturn(true);
        when(redditPostRepository.existsByUrl("https://u3")).thenReturn(false);

        // When
        List<RedditPost> out = client.searchPostsOnSubreddit("kotlin");

        // Then
        assertEquals(2, out.size(), "Powinny zostać zwrócone tylko 2 nowe posty z selftext");

        RedditPost p1 = out.get(0);
        assertEquals("T1", p1.getTitle());
        assertEquals("https://u1", p1.getUrl());
        assertEquals("desc1", p1.getDescription());
        assertEquals("kotlin", p1.getSearchedBy());
        assertEquals(5, p1.getScore());
        assertEquals("a1", p1.getAuthor());
        assertEquals(ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(100), ZoneId.of("UTC")), p1.getCreatedDate());

        RedditPost p2 = out.get(1);
        assertEquals("T4", p2.getTitle());
        assertEquals("https://u3", p2.getUrl());
        assertEquals("desc3", p2.getDescription());
        assertEquals("kotlin", p2.getSearchedBy());
        assertNotNull(p2.getCreatedDate());
        assertEquals(ZoneId.of("UTC"), p2.getCreatedDate().getZone());

        verify(redditPostRepository, times(1)).save(argThat(post -> "https://u1".equals(post.getUrl())));
        verify(redditPostRepository, times(1)).save(argThat(post -> "https://u3".equals(post.getUrl())));
        verify(redditPostRepository, never()).save(argThat(post -> "https://dup".equals(post.getUrl())));
    }

    @Test
    void shouldThrowOnNon200WithErrorBodyInMessage() throws Exception {
        // Given
        RedditClient client = new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory);

        HttpURLConnection bad = mock(HttpURLConnection.class);
        when(bad.getResponseCode()).thenReturn(400);
        InputStream err = new ByteArrayInputStream("ERR".getBytes(StandardCharsets.UTF_8));
        when(bad.getErrorStream()).thenReturn(err);

        // When & Then
        IOException ex = assertThrows(IOException.class, () -> client.readResponse(bad));
        assertTrue(ex.getMessage().contains("HTTP error code: 400"));
        assertTrue(ex.getMessage().contains("ERR"));
    }

    @Test
    void shouldSendPostWithFormBodyAndHeaders() throws Exception {
        // given
        RedditClient client = new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        OutputStream os = new ByteArrayOutputStream();
        when(conn.getOutputStream()).thenReturn(os);

        when(httpConnectionFactory.open("http://post")).thenReturn(conn);
        when(redditClientConfig.getUser().getAgent()).thenReturn("JUnit-Agent");

        // when
        client.sendHttpRequest("http://post", "POST", "Basic auth", "key=value");

        // then
        verify(conn).setRequestMethod("POST");
        verify(conn).setRequestProperty("Authorization", "Basic auth");
        verify(conn).setRequestProperty("User-Agent", "JUnit-Agent");
        verify(conn).setRequestProperty("Accept", "application/json");
        verify(conn).setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        verify(conn).setDoOutput(true);

        assertEquals("key=value", os.toString());
    }

    @Test
    void shouldNotWriteBodyWhenPostDataIsNull() throws Exception {
        // Given
        RedditClient client = new RedditClient(redditPostRepository, redditClientConfig, httpConnectionFactory);

        when(redditClientConfig.getUser().getAgent()).thenReturn("test-agent");

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionFactory.open("http://example.com")).thenReturn(conn);

        // When
        HttpURLConnection out = client.sendHttpRequest(
                "http://example.com", "POST", "Basic abc", /* postData */ null
        );

        // Then
        assertSame(conn, out);
        verify(conn).setRequestMethod("POST");
        verify(conn).setRequestProperty("Authorization", "Basic abc");
        verify(conn).setRequestProperty("User-Agent", "test-agent");
        verify(conn).setRequestProperty("Accept", "application/json");
        verify(conn, never()).getOutputStream();
    }
}
