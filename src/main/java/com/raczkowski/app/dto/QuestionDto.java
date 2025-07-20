package com.raczkowski.app.dto;

import com.raczkowski.app.enums.SurveyQuestionType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    private Long id;
    private String value;
    private SurveyQuestionType type;
    private boolean required;
    private int minSelected;
    private int maxSelected;
    private List<AnswersDto> answers;
}
