package com.raczkowski.app.article;

import com.raczkowski.app.hashtags.Hashtag;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRequest {
    private String title;

    private String content;

    private Long id;

    private String hashtags;

    public ArticleRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
