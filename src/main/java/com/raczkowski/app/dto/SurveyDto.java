package com.raczkowski.app.dto;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDto {
    private Long id;
    private String title;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime endTime;
    private AuthorDto author;
    private List<QuestionDto> questions;
    private ZonedDateTime scheduledFor;
    private boolean isActive;
}
