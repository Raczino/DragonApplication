package com.raczkowski.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ArticleDto {
    Long id;
    String title;
    String content;
    ZonedDateTime postedDate;
    UserDto userDto;

    public ArticleDto(Long id, String title, String content, ZonedDateTime postedDate, UserDto userDto) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.userDto = userDto;
    }
}
