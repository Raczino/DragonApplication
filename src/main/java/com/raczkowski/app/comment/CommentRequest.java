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
    private Long id;
    private String content;

    private int likesNumber;
}
