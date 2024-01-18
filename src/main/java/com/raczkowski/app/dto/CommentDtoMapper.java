package com.raczkowski.app.dto;

import com.raczkowski.app.comment.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentDtoMapper {
    public static CommentDto commentDtoMapper(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getPostedDate(),
                comment.getArticle().getId(),
                comment.getLikesNumber(),
                new UserDto(
                        comment.getAppUser().getId(),
                        comment.getAppUser().getFirstName(),
                        comment.getAppUser().getLastName(),
                        comment.getAppUser().getEmail(),
                        comment.getAppUser().getUserRole(),
                        comment.getAppUser().getArticlesCount(),
                        comment.getAppUser().getCommentsCount()
                )
        );
    }
}
