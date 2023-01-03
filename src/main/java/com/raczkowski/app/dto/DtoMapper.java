package com.raczkowski.app.dto;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.comment.Comment;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public static ArticleDto articleDtoMapper(Article article) {
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                new UserDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail(),
                        article.getAppUser().getUserRole(),
                        article.getAppUser().getArticlesCount(),
                        article.getAppUser().getCommentsCount()
                ));
    }

    public static CommentDto commentDtoMapper(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getPostedDate(),
                new UserDto(
                        comment.getAppUser().getId(),
                        comment.getAppUser().getFirstName(),
                        comment.getAppUser().getLastName(),
                        comment.getAppUser().getEmail(),
                        comment.getAppUser().getUserRole(),
                        comment.getAppUser().getArticlesCount(),
                        comment.getAppUser().getCommentsCount()
                ),
                comment.getArticle().getId(),
                comment.getLikesNumber());
    }
}
