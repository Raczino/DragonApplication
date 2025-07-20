package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DeletedArticleDto extends ArticleDto {
    private ZonedDateTime deletedAt;
    private AuthorDto deletedBy;
}
