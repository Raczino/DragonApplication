package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class DeletedArticleDto extends ArticleDto {
    private ZonedDateTime deletedAt;
    private AuthorDto deletedBy;

    public DeletedArticleDto(
            Long id,
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            int likesCount,
            int commentsCount,
            AuthorDto user,
            ZonedDateTime updatedAt,
            boolean isUpdated,
            ArticleStatus status,
            ZonedDateTime acceptedAt,
            AuthorDto acceptedBy,
            ZonedDateTime deletedAt,
            AuthorDto deletedBy
    ) {
        super(id, title, content, contentHtml, postedDate, likesCount, commentsCount, user, updatedAt, isUpdated, status, acceptedAt, acceptedBy);
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }
}
