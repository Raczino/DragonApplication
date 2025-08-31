package com.raczkowski.app.Reddit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.raczkowski.app.http.HttpConnectionFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class RedditClient {

    private final RedditPostRepository redditPostRepository;
    private final RedditClientConfig redditClientConfig;
    private final HttpConnectionFactory httpConnectionFactory;

    public String getAccessToken() throws IOException {
        String auth = redditClientConfig.getClient().getId() + ":" + redditClientConfig.getClient().getSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String url = "https://www.reddit.com/api/v1/access_token";
        String postData = "grant_type=client_credentials";

        HttpURLConnection conn = sendHttpRequest(url, "POST", "Basic " + encodedAuth, postData);
        String response = readResponse(conn);

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        if (!jsonResponse.has("access_token")) {
            throw new IOException("No access_token in response: " + response);
        }
        return jsonResponse.get("access_token").getAsString();
    }

    public List<RedditPost> searchPostsOnSubreddit(String keyword) throws IOException {
        String encodedQuery = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String accessToken = getAccessToken();
        String urlString = "https://oauth.reddit.com/r/" + redditClientConfig.getUser().getSubreddit() + "/search.json?q=" + encodedQuery + "&restrict_sr=true&limit=10";
        HttpURLConnection conn = sendHttpRequest(urlString, "GET", "Bearer " + accessToken, null);
        String response = readResponse(conn);
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        JsonArray posts = jsonResponse.getAsJsonObject("data").getAsJsonArray("children");
        List<RedditPost> redditPostsForArticle = new ArrayList<>();

        for (int i = 0; i < posts.size(); i++) {
            JsonObject post = posts.get(i).getAsJsonObject().getAsJsonObject("data");
            String description = (post.has("selftext") && !post.get("selftext").getAsString().isEmpty())
                    ? post.get("selftext").getAsString() : null;

            if (description != null) {
                // kluczowe pola muszą istnieć
                if (!post.has("title") || !post.has("url")) {
                    continue;
                }

                RedditPost redditPost = new RedditPost();
                redditPost.setTitle(post.get("title").getAsString());
                redditPost.setUrl(post.get("url").getAsString());
                redditPost.setDescription(description);

                if (post.has("score")) {
                    redditPost.setScore(post.get("score").getAsInt());
                }
                if (post.has("author")) {
                    redditPost.setAuthor(post.get("author").getAsString());
                }

                long createdUtc = post.has("created_utc")
                        ? post.get("created_utc").getAsLong()
                        : Instant.now().getEpochSecond();

                ZonedDateTime createdDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(createdUtc), ZoneId.of("UTC"));
                redditPost.setCreatedDate(createdDate);
                redditPost.setSearchedBy(keyword);

                if (!checkIfPostAlreadyExists(redditPost)) {
                    redditPostRepository.save(redditPost);
                    redditPostsForArticle.add(redditPost);
                }
            }
        }
        return redditPostsForArticle;
    }


    public boolean checkIfPostAlreadyExists(RedditPost newPost) {
        return redditPostRepository.existsByUrl(newPost.getUrl());
    }

    public HttpURLConnection sendHttpRequest(String urlString, String method, String authHeader, String postData) throws IOException {
        HttpURLConnection conn = httpConnectionFactory.open(urlString);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", authHeader);
        conn.setRequestProperty("User-Agent", redditClientConfig.getUser().getAgent());
        conn.setRequestProperty("Accept", "application/json");
        if ("POST".equalsIgnoreCase(method)) {
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            if (postData != null) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes());
                }
            }
        }
        return conn;
    }

    public String readResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode + ", error response: " + sb);
            }
            return sb.toString();
        }
    }
}