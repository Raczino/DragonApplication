package com.raczkowski.app.Reddit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RedditPostRepository extends JpaRepository<RedditPost, Long> {
    boolean existsByUrl(String url);
}
