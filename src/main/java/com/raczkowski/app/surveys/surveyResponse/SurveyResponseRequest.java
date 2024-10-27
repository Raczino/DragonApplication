package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SurveyResponseRequest {
    private Long surveyId;
    private Long userId;
    private List<AnswerResponseRequest> answerResponses;
}
