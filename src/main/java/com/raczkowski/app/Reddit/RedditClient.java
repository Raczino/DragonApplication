package com.raczkowski.app.Reddit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class RedditClient {
    private static final String CLIENT_ID = "u-NWTqUDIiZFi6MBJKY-7g";
    private static final String CLIENT_SECRET = "Ou7-NAUgGLb4S3qUu4uRBoegjVCLqQ";
    private static final String USER_AGENT = "dragon /u/raczino";
    private static final String SUBREDDIT = "raczino";
    private final RedditPostRepository redditPostRepository;

    public String getAccessToken() throws IOException {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String url = "https://www.reddit.com/api/v1/access_token";
        String postData = "grant_type=client_credentials";

        HttpURLConnection conn = sendHttpRequest(url, "POST", "Basic " + encodedAuth, postData);
        String response = readResponse(conn);

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse.get("access_token").getAsString();
    }

    public void searchPostsOnSubreddit(String keyword) throws IOException {
        String queryq = String.join("|", keyword);
        String encodedQuery = URLEncoder.encode(queryq, StandardCharsets.UTF_8);

        String accessToken = getAccessToken();
        String urlString = "https://oauth.reddit.com/r/" + SUBREDDIT + "/search.json?q=" + encodedQuery + "&restrict_sr=true&limit=10";
        HttpURLConnection conn = sendHttpRequest(urlString, "GET", "Bearer " + accessToken, null);
        String response = readResponse(conn);
        System.out.println(urlString);
        // Przetwarzanie odpowiedzi
        processResponse(response);
    }

    public void savePost(RedditPost newPost) {
        List<RedditPost> existingPosts = redditPostRepository.findAll();

        boolean postExists = existingPosts.stream()
                .anyMatch(existingPost -> existingPost.equals(newPost));

        if (postExists) {
            System.out.println("Post already exists in the database.");
        } else {
            redditPostRepository.save(newPost);
            System.out.println("Post saved successfully.");
        }
    }

    private void processResponse(String response) {
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        JsonArray posts = jsonResponse.getAsJsonObject("data").getAsJsonArray("children");

        for (int i = 0; i < posts.size(); i++) {
            JsonObject post = posts.get(i).getAsJsonObject().getAsJsonObject("data");
            RedditPost redditPost = new RedditPost();
            redditPost.setTitle(post.get("title").getAsString());
            redditPost.setScore(post.get("score").getAsInt());
            redditPost.setUrl(post.get("url").getAsString());
            redditPost.setDescription(post.has("selftext")
                    && !post.get("selftext").getAsString().isEmpty()
                    ? post.get("selftext").getAsString() : "No description available");
            redditPost.setAuthor(post.get("author").getAsString());
            long createdUtc = post.get("created_utc").getAsLong();
            ZonedDateTime createdDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(createdUtc), ZoneId.systemDefault());
            redditPost.setCreatedDate(createdDate);
            System.out.println("pobrałem " + posts.size() + " postow");
            System.out.println("post: " + redditPost.getId() + " tite: " + redditPost.getTitle());
//            fetchedPosts.add(redditPost);
            //savePost(redditPost);
        }
    }

    private HttpURLConnection sendHttpRequest(String urlString, String method, String authHeader, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", authHeader);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        if (method.equals("POST")) {
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

    private String readResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode + ", error response: " + response);
        }

        return response.toString();
    }
}