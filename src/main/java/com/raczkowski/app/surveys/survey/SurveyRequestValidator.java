package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Component
public class SurveyRequestValidator { //TODO: dodać walidacje na takie same odpowiedzi, powinno zwracać blad
    private final FeatureLimitHelperService featureLimitHelperService;

    public void validateSurveyRequest(SurveyRequest surveyRequest, AppUser user) {
        if (!featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.SURVEY_COUNT_PER_WEEK)) {
            throw new ResponseException("You have reached the weekly Survey limit. If you need more survey buy premium account.");
        }

        Limits limits = featureLimitHelperService.getFeaturesLimits(user.getId());

        if (surveyRequest.getEndTime() == null
                || surveyRequest.getEndTime().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new ResponseException("End time must be in the future.");
        }

        List<QuestionRequest> questions = surveyRequest.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new ResponseException("Survey must have at least one question.");
        } else if (questions.size() > limits.getSurveyQuestionLimit()) {
            throw new ResponseException("To many questions of max: " + limits.getSurveyQuestionLimit());
        }

        for (QuestionRequest question : questions) {
            validateQuestionRequest(question, limits);
        }
    }

    public void validateQuestionRequest(QuestionRequest questionRequest, Limits limits) {
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
                throw new ResponseException("MaxSelected " + questionRequest.getMaxSelected()
                        + " cannot be less than minSelected: " + questionRequest.getMinSelected());
            }
        }

        List<AnswersRequest> answers = questionRequest.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new ResponseException("Question must have at least one answer.");
        } else if (answers.size() > limits.getSurveyQuestionAnswerLimit()) {
            throw new ResponseException("To many answers of max: " + limits.getSurveyQuestionAnswerLimit());
        }

        for (AnswersRequest answer : answers) {
            validateAnswersRequest(answer);
        }
    }

    public void validateAnswersRequest(AnswersRequest answersRequest) {
        if (answersRequest.getValue() == null || answersRequest.getValue().isEmpty()) {
            throw new ResponseException("Answer value cannot be null or empty.");
        }
    }
}
