package com.raczkowski.app.dto;

import com.raczkowski.app.enums.SurveyQuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String value;
    private SurveyQuestionType type;
    private boolean required;
    private int minSelected;
    private int maxSelected;
    private List<AnswersDto> answers;
}
