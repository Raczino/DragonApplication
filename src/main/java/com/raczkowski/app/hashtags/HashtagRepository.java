package com.raczkowski.app.hashtags;

import com.raczkowski.app.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Hashtag findByTag(String tag);

    //List<Hashtag> getHashtagsByArticles(Article article);
}
