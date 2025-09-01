package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.surveys.answers.AnswersRequest;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.questions.QuestionRequest;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SurveyServiceTest {

    @Mock
    private SurveysRepository surveyRepository;
    @Mock
    private UserService userService;
    @Mock
    private SurveyRequestValidator surveyRequestValidator;
    @Mock
    private FeatureLimitHelperService featureLimitHelperService;

    @InjectMocks
    private SurveyService surveyService;

    @Test
    public void shouldCreateSurveyMapQuestionsAndAnswersValidateAndIncrementLimit() {
        // Given
        AppUser user = new AppUser();
        user.setId(77L);
        when(userService.getLoggedUser()).thenReturn(user);

        SurveyRequest req = mock(SurveyRequest.class);
        when(req.getTitle()).thenReturn("Survey T");
        when(req.getDescription()).thenReturn("Desc");
        ZonedDateTime end = ZonedDateTime.now().plusDays(7);
        when(req.getEndTime()).thenReturn(end);
        ZonedDateTime schedule = ZonedDateTime.now().plusDays(1);
        when(req.getScheduledFor()).thenReturn(schedule);

        QuestionRequest q1 = mock(QuestionRequest.class);
        when(q1.getValue()).thenReturn("Q1?");
        when(q1.getType()).thenReturn(SurveyQuestionType.SINGLE_CHOICE);
        when(q1.isRequired()).thenReturn(true);
        when(q1.getMinSelected()).thenReturn(1);
        when(q1.getMaxSelected()).thenReturn(1);

        AnswersRequest q1a1 = mock(AnswersRequest.class);
        when(q1a1.getValue()).thenReturn("A1");
        AnswersRequest q1a2 = mock(AnswersRequest.class);
        when(q1a2.getValue()).thenReturn("A2");
        when(q1.getAnswers()).thenReturn(List.of(q1a1, q1a2));

        QuestionRequest q2 = mock(QuestionRequest.class);
        when(q2.getValue()).thenReturn("Q2?");
        when(q2.getType()).thenReturn(SurveyQuestionType.MULTIPLE_CHOICE);
        when(q2.isRequired()).thenReturn(false);
        when(q2.getMinSelected()).thenReturn(0);
        when(q2.getMaxSelected()).thenReturn(3);

        AnswersRequest q2a1 = mock(AnswersRequest.class);
        when(q2a1.getValue()).thenReturn("B1");
        when(q2.getAnswers()).thenReturn(List.of(q2a1));

        when(req.getQuestions()).thenReturn(List.of(q1, q2));

        when(surveyRepository.save(any(Survey.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Survey saved = surveyService.createNewSurvey(req);

        // Then
        verify(surveyRequestValidator).validateSurveyRequest(req, user);

        assertEquals("Survey T", saved.getTitle());
        assertEquals("Desc", saved.getDescription());
        assertEquals(user, saved.getOwner());
        assertEquals(end, saved.getEndTime());
        assertEquals(schedule, saved.getScheduledFor());
        assertNotNull(saved.getCreatedAt());

        assertEquals(2, saved.getQuestions().size());

        Question qSaved1 = saved.getQuestions().get(0);
        assertEquals("Q1?", qSaved1.getValue());
        assertEquals(SurveyQuestionType.SINGLE_CHOICE, qSaved1.getType());
        assertTrue(qSaved1.isRequired());
        assertEquals(1, qSaved1.getMinSelected());
        assertEquals(1, qSaved1.getMaxSelected());
        assertSame(saved, qSaved1.getSurvey());
        assertEquals(2, qSaved1.getAnswers().size());
        assertEquals("A1", qSaved1.getAnswers().get(0).getValue());
        assertEquals("A2", qSaved1.getAnswers().get(1).getValue());
        assertSame(qSaved1, qSaved1.getAnswers().get(0).getQuestion());
        assertSame(qSaved1, qSaved1.getAnswers().get(1).getQuestion());

        Question qSaved2 = saved.getQuestions().get(1);
        assertEquals("Q2?", qSaved2.getValue());
        assertEquals(SurveyQuestionType.MULTIPLE_CHOICE, qSaved2.getType());
        assertFalse(qSaved2.isRequired());
        assertEquals(0, qSaved2.getMinSelected());
        assertEquals(3, qSaved2.getMaxSelected());
        assertSame(saved, qSaved2.getSurvey());
        assertEquals(1, qSaved2.getAnswers().size());
        assertEquals("B1", qSaved2.getAnswers().get(0).getValue());
        assertSame(qSaved2, qSaved2.getAnswers().get(0).getQuestion());

        verify(surveyRepository).save(any(Survey.class));
        verify(featureLimitHelperService).incrementFeatureUsage(77L, FeatureKeys.SURVEY_COUNT_PER_WEEK);
    }

    @Test
    public void shouldDeleteSurveyWhenFound() {
        // Given
        Survey s = new Survey();
        when(surveyRepository.findById(9L)).thenReturn(Optional.of(s));

        // When
        surveyService.deleteSurvey(9L);

        // Then
        verify(surveyRepository).delete(s);
    }

    @Test
    public void shouldThrowWhenDeletingSurveyNotFound() {
        // Given
        when(surveyRepository.findById(404L)).thenReturn(Optional.empty());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> surveyService.deleteSurvey(404L));
        assertEquals(ErrorMessages.SURVEY_NOT_FOUND, ex.getMessage());
        verify(surveyRepository, never()).delete(any());
    }

    @Test
    public void shouldReturnSurveyById() {
        // Given
        Survey s = new Survey();
        when(surveyRepository.findSurveyById(1L)).thenReturn(s);

        // When
        Survey out = surveyService.getSurveyById(1L);

        // Then
        assertSame(s, out);
        verify(surveyRepository).findSurveyById(1L);
    }

    @Test
    public void shouldThrowWhenSurveyByIdNotExists() {
        // Given
        when(surveyRepository.findSurveyById(2L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> surveyService.getSurveyById(2L));
        assertEquals(ErrorMessages.SURVEY_NOT_FOUND, ex.getMessage());
    }

    @Test
    public void shouldReturnAllSurveysFromRepository() {
        // Given
        Survey a = new Survey();
        Survey b = new Survey();
        when(surveyRepository.findAll()).thenReturn(List.of(a, b));

        // When
        List<Survey> out = surveyService.getAllSurveys();

        // Then
        assertEquals(2, out.size());
        verify(surveyRepository).findAll();
    }
}