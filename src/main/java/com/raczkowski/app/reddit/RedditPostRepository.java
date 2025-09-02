package com.raczkowski.app.reddit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedditPostRepository extends JpaRepository<RedditPost, Long> {
    boolean existsByUrl(String url);
}
