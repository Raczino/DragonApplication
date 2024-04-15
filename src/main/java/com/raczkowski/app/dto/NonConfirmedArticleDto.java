package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;

import java.time.ZonedDateTime;

public class NonConfirmedArticleDto extends ArticleDto {

    private ArticleStatus status;

    public NonConfirmedArticleDto(Long id, String title, String content, ZonedDateTime postedDate, int likesCount, authorDto user, ZonedDateTime updatedAt, boolean isUpdated, ArticleStatus status) {
        super(id, title, content, postedDate, likesCount, user, updatedAt, isUpdated, status);
    }

}
