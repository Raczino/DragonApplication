package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CommentDto {
    private Long id;

    private String content;

    private ZonedDateTime postedDate;

    private Long ArticleId;

    private int likesNumber;

    private authorDto author;

    private ZonedDateTime updatedAt;

    private boolean isUpdated;
}
