package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SurveyResponseRequest {
    private Long surveyId;
    private Long userId;
    private List<AnswerResponseRequest> answerResponses;
}
