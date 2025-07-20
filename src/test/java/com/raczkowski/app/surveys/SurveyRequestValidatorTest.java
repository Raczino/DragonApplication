package com.raczkowski.app.surveys;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import com.raczkowski.app.surveys.survey.SurveyRequest;
import com.raczkowski.app.surveys.survey.SurveyRequestValidator;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SurveyRequestValidatorTest {

    private SurveyRequestValidator surveyRequestValidator;
    private FeatureLimitHelperService featureLimitHelperService;
    private AppUser user;
    private Limits limits;

    @BeforeEach
    void setUp() {
        featureLimitHelperService = mock(FeatureLimitHelperService.class);
        MockitoAnnotations.openMocks(this);
        surveyRequestValidator = new SurveyRequestValidator(featureLimitHelperService);

        user = new AppUser();
        user.setId(1L);

        limits = new Limits(
                20,     // articleContentMinLength
                200000, // articleContentMaxLength
                8,      // articleTitleMinLength
                500,    // articleTitleMaxLength
                10,     // hashtagsMaxLength
                10,     // commentContentMinLength
                1000,   // commentContentMaxLength
                99999,  // articleLimit
                99999,  // commentLimit
                20,     // surveyLimit
                10,     // surveyQuestionLimit
                10      // surveyQuestionAnswerLimit
        );

        when(featureLimitHelperService.canUseFeature(anyLong(), eq(FeatureKeys.SURVEY_COUNT_PER_WEEK)))
                .thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(anyLong())).thenReturn(limits);
    }

    private SurveyRequest createValidSurveyRequest() {
        AnswersRequest answer = new AnswersRequest("Valid Answer");
        QuestionRequest question = new QuestionRequest(
                "Valid Question?",
                SurveyQuestionType.SINGLE_CHOICE,
                true,
                1,
                1,
                List.of(answer)
        );

        SurveyRequest request = new SurveyRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid Description");
        request.setEndTime(ZonedDateTime.now().plusDays(1));
        request.setQuestions(List.of(question));
        request.setScheduledFor(ZonedDateTime.now().plusHours(1));

        return request;
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNull() {
        // given
        SurveyRequest request = createValidSurveyRequest();
        request.setEndTime(null);

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    public void shouldPassValidationForCorrectSurveyRequest() {
        SurveyRequest surveyRequest = new SurveyRequest();
        surveyRequest.setTitle("Valid Survey Title");
        surveyRequest.setDescription("This is a valid survey description.");
        surveyRequest.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));

        AnswersRequest answer1 = new AnswersRequest();
        answer1.setValue("Answer 1");
        AnswersRequest answer2 = new AnswersRequest();
        answer2.setValue("Answer 2");

        QuestionRequest question = new QuestionRequest();
        question.setValue("Valid question?");
        question.setType(SurveyQuestionType.SINGLE_CHOICE);
        question.setAnswers(Arrays.asList(answer1, answer2));

        surveyRequest.setQuestions(Collections.singletonList(question));

        assertDoesNotThrow(() -> surveyRequestValidator.validateSurveyRequest(surveyRequest, user));
    }

    @Test
    public void shouldPassValidationForMultipleChoiceQuestion() {
        SurveyRequest surveyRequest = new SurveyRequest();
        surveyRequest.setTitle("Survey with multiple choice");
        surveyRequest.setDescription("Valid description.");
        surveyRequest.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).plusHours(5));

        AnswersRequest a1 = new AnswersRequest();
        a1.setValue("Option 1");
        AnswersRequest a2 = new AnswersRequest();
        a2.setValue("Option 2");
        AnswersRequest a3 = new AnswersRequest();
        a3.setValue("Option 3");

        QuestionRequest multiChoiceQuestion = new QuestionRequest();
        multiChoiceQuestion.setValue("Choose options");
        multiChoiceQuestion.setType(SurveyQuestionType.MULTIPLE_CHOICE);
        multiChoiceQuestion.setMinSelected(1);
        multiChoiceQuestion.setMaxSelected(2);
        multiChoiceQuestion.setAnswers(Arrays.asList(a1, a2, a3));

        surveyRequest.setQuestions(Collections.singletonList(multiChoiceQuestion));

        assertDoesNotThrow(() -> surveyRequestValidator.validateSurveyRequest(surveyRequest, user));
    }

    @Test
    public void shouldPassValidationForSingleAnswerQuestion() {
        SurveyRequest surveyRequest = new SurveyRequest();
        surveyRequest.setTitle("Single answer survey");
        surveyRequest.setDescription("Description is valid.");
        surveyRequest.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10));

        AnswersRequest answer = new AnswersRequest();
        answer.setValue("Only one answer");

        QuestionRequest question = new QuestionRequest();
        question.setValue("Single choice?");
        question.setType(SurveyQuestionType.SINGLE_CHOICE);
        question.setAnswers(Collections.singletonList(answer));

        surveyRequest.setQuestions(Collections.singletonList(question));

        assertDoesNotThrow(() -> surveyRequestValidator.validateSurveyRequest(surveyRequest, user));
    }

    @Test
    public void shouldPassValidationForValidAnswerRequest() {
        AnswersRequest answer = new AnswersRequest();
        answer.setValue("Valid Answer");

        assertDoesNotThrow(() -> surveyRequestValidator.validateAnswersRequest(answer));
    }

    @Test
    public void shouldPassValidationForValidQuestionRequest() {
        AnswersRequest answer1 = new AnswersRequest();
        answer1.setValue("Answer1");
        AnswersRequest answer2 = new AnswersRequest();
        answer2.setValue("Answer2");

        QuestionRequest question = new QuestionRequest();
        question.setValue("Is this valid?");
        question.setType(SurveyQuestionType.SINGLE_CHOICE);
        question.setAnswers(Arrays.asList(answer1, answer2));

        assertDoesNotThrow(() -> surveyRequestValidator.validateQuestionRequest(question, limits));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsInPast() {
        // given
        SurveyRequest request = createValidSurveyRequest();
        request.setEndTime(ZonedDateTime.now().minusDays(1));

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("End time must be in the future.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsNull() {
        // given
        SurveyRequest request = createValidSurveyRequest();
        request.setQuestions(null);

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsEmpty() {
        // given
        SurveyRequest request = createValidSurveyRequest();
        request.setQuestions(List.of());

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("Survey must have at least one question.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTooManyQuestions() {
        // given
        QuestionRequest question = new QuestionRequest(
                "Valid Question?",
                SurveyQuestionType.SINGLE_CHOICE,
                true,
                1,
                1,
                List.of(new AnswersRequest("A"))
        );

        List<QuestionRequest> questions = List.of(
                question, question, question, question, question,
                question, question, question, question, question, question // 11 > 10
        );

        SurveyRequest request = createValidSurveyRequest();
        request.setQuestions(questions);

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("To many questions of max: " + limits.getSurveyQuestionLimit(), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuestionHasTooManyAnswers() {
        // given
        List<AnswersRequest> answers = List.of(
                new AnswersRequest("A"),
                new AnswersRequest("B"),
                new AnswersRequest("C"),
                new AnswersRequest("D"),
                new AnswersRequest("E"),
                new AnswersRequest("F"),
                new AnswersRequest("G"),
                new AnswersRequest("H"),
                new AnswersRequest("I"),
                new AnswersRequest("J"),
                new AnswersRequest("K") // 11 > 10
        );

        QuestionRequest question = new QuestionRequest(
                "Valid Question?",
                SurveyQuestionType.SINGLE_CHOICE,
                true,
                1,
                1,
                answers
        );

        SurveyRequest request = createValidSurveyRequest();
        request.setQuestions(List.of(question));

        // when + then
        ResponseException exception = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));

        assertEquals("To many answers of max: " + limits.getSurveyQuestionAnswerLimit(), exception.getMessage());
    }

    @Test
    @DisplayName("Should throw if title is null")
    void shouldThrowWhenTitleIsNull() {
        SurveyRequest request = createValidSurveyRequest();
        request.setTitle(null);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("Title and description is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw if description is null")
    void shouldThrowWhenDescriptionIsNull() {
        SurveyRequest request = createValidSurveyRequest();
        request.setDescription(null);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("Title and description is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw if title or description too short")
    void shouldThrowWhenTitleOrDescriptionTooShort() {
        SurveyRequest request = createValidSurveyRequest();
        request.setTitle("Short");
        request.setDescription("Desc");

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));
        assertTrue(ex.getMessage().contains("lower than min length"));
    }

    @Test
    @DisplayName("Should throw if title or description too long")
    void shouldThrowWhenTitleOrDescriptionTooLong() {
        SurveyRequest request = createValidSurveyRequest();
        // title max 100, description max 256
        request.setTitle("T".repeat(101));
        request.setDescription("D".repeat(257));

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));
        assertTrue(ex.getMessage().contains("bigger than maximum length"));
    }

    @Test
    @DisplayName("Should throw if question value is null or empty")
    void shouldThrowWhenQuestionValueNullOrEmpty() {
        AnswersRequest answer = new AnswersRequest("Valid Answer");
        QuestionRequest qNull = new QuestionRequest(null, SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of(answer));
        QuestionRequest qEmpty = new QuestionRequest("", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of(answer));

        Limits lim = limits;

        ResponseException exNull = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(qNull, lim));
        assertEquals("Question value cannot be null or empty.", exNull.getMessage());

        ResponseException exEmpty = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(qEmpty, lim));
        assertEquals("Question value cannot be null or empty.", exEmpty.getMessage());
    }

    @Test
    @DisplayName("Should throw if question type is null")
    void shouldThrowWhenQuestionTypeNull() {
        AnswersRequest answer = new AnswersRequest("Answer");
        QuestionRequest question = new QuestionRequest("Valid Question", null, true, 1, 1, List.of(answer));

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(question, limits));
        assertEquals("Question type is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw if question too short or too long")
    void shouldThrowWhenQuestionTooShortOrTooLong() {
        AnswersRequest answer = new AnswersRequest("Answer");
        String shortValue = "Short";  // less than 8
        String longValue = "L".repeat(151); // more than 150

        QuestionRequest tooShort = new QuestionRequest(shortValue, SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of(answer));
        QuestionRequest tooLong = new QuestionRequest(longValue, SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of(answer));

        ResponseException exShort = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(tooShort, limits));
        assertTrue(exShort.getMessage().contains("at least"));

        ResponseException exLong = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(tooLong, limits));
        assertTrue(exLong.getMessage().contains("longer than maximum length"));
    }

    @Test
    @DisplayName("Should throw if MULTIPLE_CHOICE question minSelected < 1 or maxSelected < minSelected")
    void shouldThrowWhenMultipleChoiceMinMaxInvalid() {
        AnswersRequest answer = new AnswersRequest("Answer");
        QuestionRequest minSelectedZero = new QuestionRequest("Valid Question", SurveyQuestionType.MULTIPLE_CHOICE, true, 0, 2, List.of(answer));
        QuestionRequest maxLessThanMin = new QuestionRequest("Valid Question", SurveyQuestionType.MULTIPLE_CHOICE, true, 3, 2, List.of(answer));

        ResponseException exMin = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(minSelectedZero, limits));
        assertEquals("MinSelected cannot be lower than 1.", exMin.getMessage());

        ResponseException exMax = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(maxLessThanMin, limits));
        assertTrue(exMax.getMessage().contains("cannot be less than minSelected"));
    }

    @Test
    @DisplayName("Should throw if question answers null or empty")
    void shouldThrowWhenQuestionAnswersNullOrEmpty() {
        QuestionRequest nullAnswers = new QuestionRequest("Valid Question", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, null);
        QuestionRequest emptyAnswers = new QuestionRequest("Valid Question", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of());

        ResponseException exNull = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(nullAnswers, limits));
        assertEquals("Question must have at least one answer.", exNull.getMessage());

        ResponseException exEmpty = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(emptyAnswers, limits));
        assertEquals("Question must have at least one answer.", exEmpty.getMessage());
    }

    @Test
    @DisplayName("Should throw if question answers contain duplicates")
    void shouldThrowWhenDuplicateAnswers() {
        AnswersRequest answer1 = new AnswersRequest("Answer");
        AnswersRequest answer2 = new AnswersRequest("answer");
        QuestionRequest question = new QuestionRequest("Valid Question", SurveyQuestionType.SINGLE_CHOICE, true, 1, 1, List.of(answer1, answer2));

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateQuestionRequest(question, limits));
        assertEquals("Response must be unique. Duplicated found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw if answer value is null or empty")
    void shouldThrowWhenAnswerValueNullOrEmpty() {
        AnswersRequest nullValue = new AnswersRequest(null);
        AnswersRequest emptyValue = new AnswersRequest("");

        ResponseException exNull = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateAnswersRequest(nullValue));
        assertEquals("Answer value is required", exNull.getMessage());

        ResponseException exEmpty = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateAnswersRequest(emptyValue));
        assertEquals("Answer value is required", exEmpty.getMessage());
    }

    @Test
    @DisplayName("Should throw if answer value too long")
    void shouldThrowWhenAnswerTooLong() {
        String longAnswer = "A".repeat(101);
        AnswersRequest answer = new AnswersRequest(longAnswer);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateAnswersRequest(answer));
        assertTrue(ex.getMessage().contains("bigger than maximum length"));
    }

    @Test
    @DisplayName("Should throw if user reached survey count limit")
    void shouldThrowWhenUserReachedSurveyCountLimit() {
        when(featureLimitHelperService.canUseFeature(anyLong(), eq(FeatureKeys.SURVEY_COUNT_PER_WEEK)))
                .thenReturn(false);

        SurveyRequest request = createValidSurveyRequest();

        ResponseException ex = assertThrows(ResponseException.class, () ->
                surveyRequestValidator.validateSurveyRequest(request, user));
        assertEquals("You have reached the weekly Survey limit. If you need more survey buy premium account.", ex.getMessage());
    }
}
