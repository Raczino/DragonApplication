package com.raczkowski.app.article;

import lombok.*;

@Getter
@AllArgsConstructor
public class ArticleRequest {
    private final String title;

    private final String content;

    private Long id;

    public ArticleRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
