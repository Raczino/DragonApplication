package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ArticleDto {
    Long id;

    String title;

    String content;

    ZonedDateTime postedDate;

    int likesCount;

    UserDto user;

    ZonedDateTime updatedAt;
}
