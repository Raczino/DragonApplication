package com.raczkowski.app.Reddit;

import lombok.AllArgsConstructor;
import org.hibernate.mapping.Collection;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class RedditScheduler {
    private final RedditPostService redditPostService;
    @Scheduled(fixedRate = 6000) //60min
    public void fetchPosts() {
        try {
            redditPostService.getCommentsForArticle();
        } catch (IOException e) {
            System.err.println("Błąd podczas odpytywania API: " + e.getMessage());
        }
    }
}
