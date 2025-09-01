package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Component
public class SurveyRequestValidator {
    private final FeatureLimitHelperService featureLimitHelperService;
    private final int MIN_LENGTH = 8;

    public void validateSurveyRequest(SurveyRequest surveyRequest, AppUser user) {
        if (!featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.SURVEY_COUNT_PER_WEEK)) {
            throw new ResponseException(ErrorMessages.LIMIT_REACHED);
        }

        Limits limits = featureLimitHelperService.getFeaturesLimits(user.getId());

        int surveyDescriptionMaxLength = 256;
        int surveyTitleMaxLength = 100;
        if (null == surveyRequest.getTitle() || null == surveyRequest.getDescription()) {
            throw new ResponseException(ErrorMessages.REQUIRED_TITLE_AND_DESCRIPTION);
        } else if (surveyRequest.getTitle().length() < MIN_LENGTH || surveyRequest.getDescription().length() < MIN_LENGTH) {
            throw new ResponseException(ErrorMessages.TITLE_AND_DESCRIPTION_TOO_SHORT + MIN_LENGTH);
        } else if (surveyRequest.getTitle().length() > surveyTitleMaxLength || surveyRequest.getDescription().length() > surveyDescriptionMaxLength) {
            throw new ResponseException(ErrorMessages.TITLE_AND_DESCRIPTION_TOO_LONG + surveyTitleMaxLength + ". " + surveyDescriptionMaxLength);
        }

        if (null == surveyRequest.getEndTime()
                || surveyRequest.getEndTime().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new ResponseException(ErrorMessages.END_DATE_MUST_BE_IN_THE_FUTURE);
        }

        List<QuestionRequest> questions = surveyRequest.getQuestions();
        if (null == questions || questions.isEmpty()) {
            throw new ResponseException(ErrorMessages.SURVEY_MUST_HAS_QUESTION);
        } else if (questions.size() > limits.getSurveyQuestionLimit()) {
            throw new ResponseException(ErrorMessages.TO_MANY_QUESTIONS + limits.getSurveyQuestionLimit());
        }

        for (QuestionRequest question : questions) {
            validateQuestionRequest(question, limits);
        }
    }

    public void validateQuestionRequest(QuestionRequest questionRequest, Limits limits) {
        if (null == questionRequest.getValue() || questionRequest.getValue().isEmpty()) {
            throw new ResponseException(ErrorMessages.QUESTION_VALUE_IS_NULL);
        }

        if (null == questionRequest.getType()) {
            throw new ResponseException(ErrorMessages.QUESTION_TYPE_IS_REQUIRED);
        }

        int questionMaxLength = 150;
        if (questionRequest.getValue().length() < MIN_LENGTH) {
            throw new ResponseException(ErrorMessages.QUESTION_VALUE_TOO_SHORT + MIN_LENGTH);
        } else if (questionRequest.getValue().length() > questionMaxLength) {
            throw new ResponseException(ErrorMessages.QUESTION_VALUE_TOO_LONG + questionMaxLength);
        }

        if (questionRequest.getType() == SurveyQuestionType.MULTIPLE_CHOICE) {
            if (questionRequest.getMinSelected() < 1) {
                throw new ResponseException(ErrorMessages.MIN_SELECTED_TOO_LOW);
            }
            if (questionRequest.getMaxSelected() < questionRequest.getMinSelected()) {
                throw new ResponseException("MaxSelected " + questionRequest.getMaxSelected()
                        + " cannot be less than minSelected: " + questionRequest.getMinSelected());
            }
        }

        List<AnswersRequest> answers = questionRequest.getAnswers();
        if (null == answers || answers.isEmpty()) {
            throw new ResponseException(ErrorMessages.QUESTION_ANSWER_REQUIRED);
        } else if (answers.size() > limits.getSurveyQuestionAnswerLimit()) {
            throw new ResponseException("To many answers of max: " + limits.getSurveyQuestionAnswerLimit());
        }

        Set<String> uniqueAnswers = new HashSet<>();
        for (AnswersRequest answer : answers) {
            validateAnswersRequest(answer);
            if (!uniqueAnswers.add(answer.getValue().toLowerCase())) {
                throw new ResponseException(ErrorMessages.ANSWER_MUST_BE_UNIQUE);
            }
        }
    }

    public void validateAnswersRequest(AnswersRequest answersRequest) {
        if (null == answersRequest.getValue() || answersRequest.getValue().isEmpty()) {
            throw new ResponseException(ErrorMessages.ANSWER_VALUE_IS_REQUIRED);
        }

        int answerMaxLength = 100;
        if (answersRequest.getValue().length() > answerMaxLength) {
            throw new ResponseException(ErrorMessages.ANSWER_VALUE_TOO_LONG + answerMaxLength);
        }
    }
}
