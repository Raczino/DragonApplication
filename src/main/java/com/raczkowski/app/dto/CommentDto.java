package com.raczkowski.app.dto;

import com.raczkowski.app.article.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CommentDto {
    private Long id;
    private String content;

    private ZonedDateTime postedDate;

    private UserDto author;

    private Long ArticleId;

    private int likesNumber;
}
