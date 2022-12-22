package com.raczkowski.app.comment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentRequest {
    private Long idOfArticle;
    private String content;
}
