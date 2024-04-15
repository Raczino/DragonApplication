package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;

import java.time.ZonedDateTime;

public class RejectedArticleDto extends ArticleDto {
    ArticleStatus status;

    public RejectedArticleDto(Long id, String title, String content, ZonedDateTime postedDate, authorDto user, ArticleStatus status) {
        super(id, title, content, postedDate, user, status);
    }
}
