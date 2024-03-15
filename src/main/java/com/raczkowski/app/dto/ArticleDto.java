package com.raczkowski.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


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

    private boolean isLiked;

    private int commentsNumber;

    public ArticleDto
            (
                    Long id,
                    String title,
                    String content,
                    ZonedDateTime postedDate,
                    int likesCount,
                    authorDto user,
                    ZonedDateTime updatedAt,
                    boolean isUpdated
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.user = user;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
    }

    public ArticleDto
            (
                    Long id,
                    String title,
                    String content,
                    ZonedDateTime postedDate,
                    int likesCount,
                    authorDto user,
                    ZonedDateTime updatedAt,
                    boolean isUpdated,
                    boolean isLiked,
                    int commentsNumber
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.user = user;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.isLiked = isLiked;
        this.commentsNumber = commentsNumber;
    }
}
