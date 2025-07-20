package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.dto.CommentDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CommentDtoMapper {
    private final AuthorDtoMapper authorDtoMapper;

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postedDate(comment.getPostedDate())
                .articleId(comment.getArticle().getId())
                .likesNumber(comment.getLikesCount())
                .author(authorDtoMapper.toAuthorDto(comment.getAppUser()))
                .isUpdated(comment.isUpdated())
                .isPinned(comment.isPinned())
                .build();
    }
}
