package com.raczkowski.app.likes;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.comment.Comment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LikesService {

    private final ArticleLikeRepository articleLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    public int getLikesCountForArticle(Article article){
        return articleLikeRepository.findAllByArticle(article).size();
    }

    public int getLikesCountForComment(Comment comment){
        return commentLikeRepository.findAllByComment(comment).size();
    }
}
