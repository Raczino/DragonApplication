package com.raczkowski.app.article;

import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ArticleRequest {
    private String title;
    private String content;
}
