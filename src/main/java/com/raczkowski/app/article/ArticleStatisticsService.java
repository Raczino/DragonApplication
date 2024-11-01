package com.raczkowski.app.article;

import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.likes.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleStatisticsService {
    private final CommentService commentService;
    private final LikesService likesService;
    public int getLikesCountForArticle(Article article) {
        return likesService.getLikesCountForArticle(article);
    }
    public int getCommentsCountForArticle(Article article){
        return commentService.getCommentCountForArticle(article);
    }
}
