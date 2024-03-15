package com.raczkowski.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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

    private boolean isLiked;

    public CommentDto
            (
                    Long id,
                    String content,
                    ZonedDateTime postedDate,
                    Long articleId,
                    int likesNumber,
                    authorDto author,
                    ZonedDateTime updatedAt,
                    boolean isUpdated
            ) {
        this.id = id;
        this.content = content;
        this.postedDate = postedDate;
        ArticleId = articleId;
        this.likesNumber = likesNumber;
        this.author = author;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
    }

    public CommentDto
            (
                    Long id,
                    String content,
                    ZonedDateTime postedDate,
                    Long articleId,
                    int likesNumber,
                    authorDto author,
                    ZonedDateTime updatedAt,
                    boolean isUpdated,
                    boolean isLiked
            ) {
        this.id = id;
        this.content = content;
        this.postedDate = postedDate;
        ArticleId = articleId;
        this.likesNumber = likesNumber;
        this.author = author;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.isLiked = isLiked;
    }
}
