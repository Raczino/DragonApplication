package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private String contentHtml;
    private ZonedDateTime postedDate;
    private int likesCount;
    private int commentsCount;
    private AuthorDto author;
    private ZonedDateTime updatedAt;
    private boolean isUpdated;
    private boolean isLiked;
    private ArticleStatus status;
    private ZonedDateTime acceptedAt;
    private AuthorDto acceptedBy;
    private boolean isPinned;
    private AuthorDto pinnedBy;
}
