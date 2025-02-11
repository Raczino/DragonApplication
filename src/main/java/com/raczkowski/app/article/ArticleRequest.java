package com.raczkowski.app.article;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRequest {
    private String title;
    private String content;
    private String contentHtml;
    private Long id;
    private String hashtags;
    private ZonedDateTime scheduledForDate;

    public ArticleRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
