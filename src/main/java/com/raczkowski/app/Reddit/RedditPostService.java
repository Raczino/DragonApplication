package com.raczkowski.app.Reddit;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.hashtags.HashtagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Service
public class RedditPostService {

    private final RedditPostRepository redditPostRepository;
    private final CommentService commentService;
    private final ArticleService articleService;
    private final HashtagService hashtagService;
    private final RedditClient redditClient;

    public void getCommentsForArticle() throws IOException {
    }
}
