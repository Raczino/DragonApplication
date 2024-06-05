package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RejectedArticleDto {
    private Long id;

    private String title;

    private String content;

    private ZonedDateTime postedDate;

    private AuthorDto user;

    private ArticleStatus status;

    private ZonedDateTime rejectedAt;

    private AuthorDto rejectedBy;
}
