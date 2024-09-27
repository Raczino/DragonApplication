package com.raczkowski.app.Reddit;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class RedditScheduler {
    private final RedditPostService redditPostService;
    @Scheduled(fixedRate = 60000) //60min
    public void fetchPosts() {
        try {
            redditPostService.getCommentsForArticle();
        } catch (IOException e) {
            System.err.println("Api connection error: " + e.getMessage());
        }
    }
}
