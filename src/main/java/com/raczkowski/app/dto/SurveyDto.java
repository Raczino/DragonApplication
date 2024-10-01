package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDto {
    private Long id;
    private ZonedDateTime createdAt;
    private ZonedDateTime endTime;
    private AuthorDto author;
    private List<QuestionDto> questions;
}
