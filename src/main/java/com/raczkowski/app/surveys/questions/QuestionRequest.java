package com.raczkowski.app.surveys.questions;

import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {
    private String value;
    private SurveyQuestionType type;
    private boolean required;
    private int minSelected;
    private int maxSelected;
    private List<AnswersRequest> answers;
}
