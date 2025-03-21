package com.raczkowski.app.surveys;

import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import com.raczkowski.app.surveys.survey.SurveyRequest;
import com.raczkowski.app.surveys.survey.SurveyRequestValidator;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SurveyRequestValidatorTest {
    @Mock
    SurveyRequestValidator surveyRequestValidator;
    @Mock
    FeatureLimitHelperService featureLimitHelperService;

    @Mock
    AppUser user = new AppUser();

    @Mock
    Limits limits = featureLimitHelperService.getFeaturesLimits(1L);

    @Test
    void shouldValidateSurveyRequestWithValidData() {
        AppUser user = new AppUser();
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), List.of(
                new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Red"), new AnswersRequest("Blue")))
        ));
        assertDoesNotThrow(() -> surveyRequestValidator.validateSurveyRequest(request, user));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNull() {
        SurveyRequest request = new SurveyRequest(null, List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsInThePast() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().minusDays(1), List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoQuestionsProvided() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), null);
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionsListIsEmpty() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), List.of());
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldValidateQuestionRequestWithValidData() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red"), new AnswersRequest("Blue")));
        assertDoesNotThrow(() -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
    }

    @Test
    void shouldThrowExceptionWhenQuestionValueIsNull() {
        QuestionRequest questionRequest = new QuestionRequest(null, SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionValueIsEmpty() {
        QuestionRequest questionRequest = new QuestionRequest("", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionTypeIsNull() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", null, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question type cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoAnswersProvided() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                null);
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersListIsEmpty() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of());
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldValidateAnswersRequestWithValidData() {
        AnswersRequest answersRequest = new AnswersRequest("Red");
        assertDoesNotThrow(() -> surveyRequestValidator.validateAnswersRequest(answersRequest));
    }

    @Test
    void shouldThrowExceptionWhenAnswerValueIsNull() {
        AnswersRequest answersRequest = new AnswersRequest(null);
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswerValueIsEmpty() {
        AnswersRequest answersRequest = new AnswersRequest("");
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNow() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now(), List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionHasNoRequiredAnswers() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                null);
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionHasEmptyAnswersList() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of());
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateQuestionRequest(questionRequest, limits));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersRequestHasNullValue() {
        AnswersRequest answersRequest = new AnswersRequest(null);
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersRequestHasEmptyValue() {
        AnswersRequest answersRequest = new AnswersRequest("");
        Exception exception = assertThrows(ResponseException.class, () -> surveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

}
