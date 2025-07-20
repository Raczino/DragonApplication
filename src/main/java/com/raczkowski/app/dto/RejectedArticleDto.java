package com.raczkowski.app.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RejectedArticleDto extends ArticleDto {
    private ZonedDateTime rejectedAt;
    private AuthorDto rejectedBy;
}
