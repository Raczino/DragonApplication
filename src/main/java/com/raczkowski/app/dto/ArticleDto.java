package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
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

    ArticleStatus status;

    ZonedDateTime acceptedAt;

    authorDto acceptedBy;

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
                    ArticleStatus status,
                    ZonedDateTime acceptedAt,
                    authorDto acceptedBy
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.user = user;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
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
                    ArticleStatus status,
                    boolean isLiked,
                    int commentsNumber,
                    ZonedDateTime acceptedAt,
                    authorDto acceptedBy
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.user = user;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.status = status;
        this.isLiked = isLiked;
        this.commentsNumber = commentsNumber;
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
    }

    public ArticleDto(
            Long id,
            String title,
            String content,
            ZonedDateTime postedDate,
            authorDto user,
            ArticleStatus status
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.user = user;
        this.status = status;
    }
}
