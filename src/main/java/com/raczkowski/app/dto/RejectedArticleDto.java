package com.raczkowski.app.dto;

import com.raczkowski.app.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class RejectedArticleDto extends ArticleDto {
    private ZonedDateTime rejectedAt;
    private AuthorDto rejectedBy;

    public RejectedArticleDto(
            Long id,
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            AuthorDto author,
            ArticleStatus status,
            ZonedDateTime rejectedAt,
            AuthorDto rejectedBy
    ) {
        super(id, title, content, contentHtml, postedDate, author, status);
        this.rejectedAt = rejectedAt;
        this.rejectedBy = rejectedBy;
    }
}
