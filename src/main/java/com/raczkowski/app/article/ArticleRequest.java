package com.raczkowski.app.article;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ArticleRequest {
    @NonNull
    private String title;
    @NonNull
    private String content;
}
