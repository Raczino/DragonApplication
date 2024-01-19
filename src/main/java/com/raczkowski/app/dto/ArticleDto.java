package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ArticleDto {
    private Long id;

    private String title;

    private String content;

    private ZonedDateTime postedDate;

    private int likesCount;

    private authorDto user;

    private ZonedDateTime updatedAt;

    private boolean isUpdated;
}
