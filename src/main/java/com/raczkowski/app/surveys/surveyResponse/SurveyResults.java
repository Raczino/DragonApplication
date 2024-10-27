package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SurveyResults {
    private String surveyTitle;
    private String surveyDescription;
    private int totalAnswers;
    private ZonedDateTime endTime;
    private List<QuestionResult> questionResults;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class QuestionResult {
        private String questionText;
        private List<AnswerResult> answerResults;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AnswerResult {
        private String answerText;
        private int count;
    }
}
