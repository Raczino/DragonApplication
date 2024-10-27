package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class SurveyRequestValidator {
    private static final int MAX_QUESTIONS = 5;
    private static final int MAX_ANSWERS = 5;

    public static void validateSurveyRequest(SurveyRequest surveyRequest) {
        if (surveyRequest.getEndTime() == null || surveyRequest.getEndTime().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new ResponseException("End time must be in the future.");
        }

        List<QuestionRequest> questions = surveyRequest.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new ResponseException("Survey must have at least one question.");
        } else if (questions.size() > MAX_QUESTIONS) {
            throw new ResponseException("To many questions of max: " + MAX_QUESTIONS);
        }

        for (QuestionRequest question : questions) {
            validateQuestionRequest(question);
        }
    }

    public static void validateQuestionRequest(QuestionRequest questionRequest) {
        if (questionRequest.getValue() == null || questionRequest.getValue().isEmpty()) {
            throw new ResponseException("Question value cannot be null or empty.");
        }

        if (questionRequest.getType() == null) {
            throw new ResponseException("Question type cannot be null.");
        }

        if (questionRequest.getType() == SurveyQuestionType.MULTIPLE_CHOICE) {
            if (questionRequest.getMinSelected() < 1) {
                throw new ResponseException("MinSelected cannot be lower than 1.");
            }
            if (questionRequest.getMaxSelected() < questionRequest.getMinSelected()) {
                throw new ResponseException("MaxSelected "+ questionRequest.getMaxSelected() +" cannot be less than minSelected: "+ questionRequest.getMinSelected());
            }
        }

        List<AnswersRequest> answers = questionRequest.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new ResponseException("Question must have at least one answer.");
        } else if (answers.size() > MAX_ANSWERS) {
            throw new ResponseException("To many answers of max: " + MAX_ANSWERS);
        }

        for (AnswersRequest answer : answers) {
            validateAnswersRequest(answer);
        }
    }

    public static void validateAnswersRequest(AnswersRequest answersRequest) {
        if (answersRequest.getValue() == null || answersRequest.getValue().isEmpty()) {
            throw new ResponseException("Answer value cannot be null or empty.");
        }
    }
}
