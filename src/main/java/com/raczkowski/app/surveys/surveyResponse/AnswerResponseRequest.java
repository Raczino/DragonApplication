package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AnswerResponseRequest {
    private Long questionId;
    private List<String> answerValues;
}
