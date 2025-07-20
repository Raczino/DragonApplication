package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
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
}
