package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dto.AuthorDto;
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
                new AuthorDto(
                        comment.getAppUser().getId(),
                        comment.getAppUser().getFirstName(),
                        comment.getAppUser().getLastName(),
                        comment.getAppUser().getEmail(),
                        comment.getAppUser().isAccountBlocked()
                ),
                comment.getPostedDate(),
                comment.isUpdated()
        );
    }

    public static CommentDto commentDtoMapperWithAdditionalFields(Comment comment, boolean isLiked) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getPostedDate(),
                comment.getArticle().getId(),
                comment.getLikesNumber(),
                new AuthorDto(
                        comment.getAppUser().getId(),
                        comment.getAppUser().getFirstName(),
                        comment.getAppUser().getLastName(),
                        comment.getAppUser().getEmail(),
                        comment.getAppUser().isAccountBlocked()
                ),
                comment.getPostedDate(),
                comment.isUpdated(),
                isLiked
        );
    }
}
