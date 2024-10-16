package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SurveyRequestValidatorTest {

    @Test
    void shouldValidateSurveyRequestWithValidData() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), List.of(
                new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Red"), new AnswersRequest("Blue")))
        ));
        assertDoesNotThrow(() -> SurveyRequestValidator.validateSurveyRequest(request));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNull() {
        SurveyRequest request = new SurveyRequest(null, List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateSurveyRequest(request));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsInThePast() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().minusDays(1), List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateSurveyRequest(request));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoQuestionsProvided() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), null);
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateSurveyRequest(request));
        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionsListIsEmpty() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now().plusDays(1), List.of());
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateSurveyRequest(request));
        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldValidateQuestionRequestWithValidData() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red"), new AnswersRequest("Blue")));
        assertDoesNotThrow(() -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
    }

    @Test
    void shouldThrowExceptionWhenQuestionValueIsNull() {
        QuestionRequest questionRequest = new QuestionRequest(null, SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionValueIsEmpty() {
        QuestionRequest questionRequest = new QuestionRequest("", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionTypeIsNull() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", null, true, 1, 1,
                List.of(new AnswersRequest("Red")));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question type cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoAnswersProvided() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                null);
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersListIsEmpty() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of());
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldValidateAnswersRequestWithValidData() {
        AnswersRequest answersRequest = new AnswersRequest("Red");
        assertDoesNotThrow(() -> SurveyRequestValidator.validateAnswersRequest(answersRequest));
    }

    @Test
    void shouldThrowExceptionWhenAnswerValueIsNull() {
        AnswersRequest answersRequest = new AnswersRequest(null);
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswerValueIsEmpty() {
        AnswersRequest answersRequest = new AnswersRequest("");
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNow() {
        SurveyRequest request = new SurveyRequest(ZonedDateTime.now(), List.of(
                new QuestionRequest("Question?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                        List.of(new AnswersRequest("Answer")))
        ));
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateSurveyRequest(request));
        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionHasNoRequiredAnswers() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                null); // Brak odpowiedzi
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionHasEmptyAnswersList() {
        QuestionRequest questionRequest = new QuestionRequest("What is your favorite color?", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1,
                List.of()); // Pusta lista odpowiedzi
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateQuestionRequest(questionRequest));
        assertEquals("Question must have at least one answer.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersRequestHasNullValue() {
        AnswersRequest answersRequest = new AnswersRequest(null); // Null value
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAnswersRequestHasEmptyValue() {
        AnswersRequest answersRequest = new AnswersRequest(""); // Pusta wartość
        Exception exception = assertThrows(ResponseException.class, () -> SurveyRequestValidator.validateAnswersRequest(answersRequest));
        assertEquals("Answer value cannot be null or empty.", exception.getMessage());
    }

}
