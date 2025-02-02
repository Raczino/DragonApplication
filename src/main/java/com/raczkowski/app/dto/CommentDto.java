package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private ZonedDateTime postedDate;
    private Long articleId;
    private int likesNumber;
    private AuthorDto author;
    private ZonedDateTime updatedAt;
    private boolean isUpdated;
    private boolean isLiked;
    private boolean isPinned;

    public CommentDto
            (
                    Long id,
                    String content,
                    ZonedDateTime postedDate,
                    Long articleId,
                    int likesNumber,
                    AuthorDto author,
                    ZonedDateTime updatedAt,
                    boolean isUpdated,
                    boolean isPinned
            ) {
        this.id = id;
        this.content = content;
        this.postedDate = postedDate;
        this.articleId = articleId;
        this.likesNumber = likesNumber;
        this.author = author;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.isPinned = isPinned;
    }
}
