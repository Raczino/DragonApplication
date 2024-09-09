package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


@Getter
@Setter
@AllArgsConstructor
public class ArticleDto {
    private Long id;

    private String title;

    private String content;

    private ZonedDateTime postedDate;

    private int likesCount;

    private AuthorDto author;

    private ZonedDateTime updatedAt;

    private boolean isUpdated;

    private boolean isLiked;

    private int commentsNumber;

    private ArticleStatus status;

    private ZonedDateTime acceptedAt;

    private AuthorDto acceptedBy;

    private boolean isPinned;

    public ArticleDto
            (
                    Long id,
                    String title,
                    String content,
                    ZonedDateTime postedDate,
                    int likesCount,
                    AuthorDto author,
                    ZonedDateTime updatedAt,
                    boolean isUpdated,
                    ArticleStatus status,
                    ZonedDateTime acceptedAt,
                    AuthorDto acceptedBy
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.author = author;
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
                    AuthorDto author,
                    ZonedDateTime updatedAt,
                    boolean isUpdated,
                    ArticleStatus status,
                    boolean isLiked,
                    int commentsNumber,
                    ZonedDateTime acceptedAt,
                    AuthorDto acceptedBy,
                    boolean isPinned
            ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.likesCount = likesCount;
        this.author = author;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.status = status;
        this.isLiked = isLiked;
        this.commentsNumber = commentsNumber;
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
        this.isPinned = isPinned;
    }

    public ArticleDto(
            Long id,
            String title,
            String content,
            ZonedDateTime postedDate,
            AuthorDto author,
            ArticleStatus status
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.author = author;
        this.status = status;
    }
}
