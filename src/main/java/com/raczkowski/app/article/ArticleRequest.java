package com.raczkowski.app.article;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRequest {
    private String title;

    private String content;

    private Long id;

    public ArticleRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
