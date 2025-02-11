package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class NonConfirmedArticleDto extends ArticleDto {
    public NonConfirmedArticleDto(
            Long id,
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            AuthorDto user,
            ArticleStatus status
    ) {
        super(id, title, content, contentHtml, postedDate, user, status);
    }
}
