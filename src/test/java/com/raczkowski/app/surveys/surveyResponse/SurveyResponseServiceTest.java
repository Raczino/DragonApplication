package com.raczkowski.app.surveys.surveyResponse;

import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.Answers;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;
import com.raczkowski.app.surveys.survey.SurveysRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SurveyResponseServiceTest {

    @Mock
    private SurveysRepository surveysRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyResponseRepository surveyResponseRepository;
    @Mock
    private AnswerResponseValidator answerResponseValidator;

    @InjectMocks
    private SurveyResponseService surveyResponseService;

    @Test
    void saveSurveyResponse_happyPath() {
        Survey survey = new Survey();
        survey.setId(1L);
        Question q = new Question();
        q.setId(10L);
        q.setValue("Q1");
        q.setRequired(true);
        q.setAnswers(List.of(new Answers("A", q)));
        survey.setQuestions(List.of(q));

        when(surveysRepository.findSurveyById(1L)).thenReturn(survey);

        AppUser u = new AppUser();
        u.setId(2L);
        when(userRepository.getAppUserById(2L)).thenReturn(u);

        when(surveyResponseRepository.existsBySurveyAndUser(survey, u)).thenReturn(false);

        AnswerResponseRequest ar = new AnswerResponseRequest();
        ar.setQuestionId(10L);
        ar.setAnswerValues(List.of("A"));

        SurveyResponseRequest req = new SurveyResponseRequest();
        req.setSurveyId(1L);
        req.setUserId(2L);
        req.setAnswerResponses(List.of(ar));

        surveyResponseService.saveSurveyResponse(req);

        verify(answerResponseValidator).checkResponseDate(survey);
        verify(answerResponseValidator).validateRequiredQuestions(anyList(), anyMap());
        verify(answerResponseValidator).validateAnswerResponse(ar, survey.getQuestions());
        verify(answerResponseValidator).findQuestionById(10L, survey.getQuestions());
        verify(surveyResponseRepository).save(any(SurveyResponse.class));
    }

    @Test
    void saveSurveyResponse_shouldThrowOnDuplicate() {
        Survey s = new Survey();
        s.setId(1L);
        s.setQuestions(List.of(new Question()));

        when(surveysRepository.findSurveyById(1L)).thenReturn(s);
        AppUser u = new AppUser();
        u.setId(2L);
        when(userRepository.getAppUserById(2L)).thenReturn(u);

        when(surveyResponseRepository.existsBySurveyAndUser(s, u)).thenReturn(true);

        SurveyResponseRequest req = new SurveyResponseRequest();
        req.setSurveyId(1L);
        req.setUserId(2L);
        req.setAnswerResponses(List.of(new AnswerResponseRequest(10L, List.of("A"))));

        ResponseException ex = assertThrows(ResponseException.class, () -> surveyResponseService.saveSurveyResponse(req));
        assertTrue(ex.getMessage().contains("already answered"));
        verify(surveyResponseRepository, never()).save(any());
    }

    @Test
    void getSurveyResults_aggregatesCounts() {
        Survey s = new Survey();
        s.setId(1L);
        s.setTitle("T");
        s.setDescription("D");
        s.setEndTime(ZonedDateTime.now());

        Question q = new Question();
        q.setValue("Q");
        Answers a1 = new Answers("A1", q);
        Answers a2 = new Answers("A2", q);
        q.setAnswers(List.of(a1, a2));
        s.setQuestions(List.of(q));

        when(surveysRepository.findSurveyById(1L)).thenReturn(s);
        when(surveyResponseRepository.countByQuestionAndAnswer(q, "A1")).thenReturn(3);
        when(surveyResponseRepository.countByQuestionAndAnswer(q, "A2")).thenReturn(1);
        when(surveyResponseRepository.countResponsesBySurveyId(1L)).thenReturn(4);

        SurveyResults out = surveyResponseService.getSurveyResults(1L);

        assertEquals("T", out.getSurveyTitle());
        assertEquals(4, out.getTotalAnswers());
        assertEquals(1, out.getQuestionResults().size());
        assertEquals(2, out.getQuestionResults().get(0).getAnswerResults().size());
    }


    @Test
    public void shouldThrowWhenSurveyOrUserNotFound() {
        // Given
        SurveyResponseRequest req = mock(SurveyResponseRequest.class);
        when(req.getSurveyId()).thenReturn(1L);
        when(req.getUserId()).thenReturn(2L);

        when(surveysRepository.findSurveyById(1L)).thenReturn(null);
        when(userRepository.getAppUserById(2L)).thenReturn(new AppUser());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> surveyResponseService.saveSurveyResponse(req));
        assertEquals(ErrorMessages.SURVEY_OR_USER_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(answerResponseValidator, surveyResponseRepository);
    }

    @Test
    public void shouldSaveSurveyResponse_MapAnswers_ValidateAndPersist() {
        // Given
        AppUser user = new AppUser();
        user.setId(10L);

        Survey survey = new Survey();
        survey.setId(5L);
        survey.setTitle("S1");
        survey.setDescription("D1");
        survey.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(3));

        Question qReq = new Question();
        qReq.setId(100L);
        qReq.setValue("Required?");
        qReq.setRequired(true);
        qReq.setAnswers(List.of(
                new Answers("A", qReq),
                new Answers("B", qReq)
        ));

        Question qOpt = new Question();
        qOpt.setId(200L);
        qOpt.setValue("Optional?");
        qOpt.setRequired(false);
        qOpt.setAnswers(List.of(
                new Answers("X", qOpt)
        ));

        survey.setQuestions(List.of(qReq, qOpt));

        AnswerResponseRequest ar1 = mock(AnswerResponseRequest.class);
        when(ar1.getQuestionId()).thenReturn(100L);
        when(ar1.getAnswerValues()).thenReturn(List.of("A"));

        AnswerResponseRequest ar2 = mock(AnswerResponseRequest.class);
        when(ar2.getQuestionId()).thenReturn(200L);
        when(ar2.getAnswerValues()).thenReturn(null);

        SurveyResponseRequest req = mock(SurveyResponseRequest.class);
        when(req.getSurveyId()).thenReturn(5L);
        when(req.getUserId()).thenReturn(10L);
        when(req.getAnswerResponses()).thenReturn(List.of(ar1, ar2));

        when(surveysRepository.findSurveyById(5L)).thenReturn(survey);
        when(userRepository.getAppUserById(10L)).thenReturn(user);

        when(answerResponseValidator.findQuestionById(100L, survey.getQuestions())).thenReturn(qReq);
        when(answerResponseValidator.findQuestionById(200L, survey.getQuestions())).thenReturn(qOpt);

        when(surveyResponseRepository.existsBySurveyAndUser(survey, user)).thenReturn(false);

        ArgumentCaptor<SurveyResponse> captor = ArgumentCaptor.forClass(SurveyResponse.class);
        when(surveyResponseRepository.save(any(SurveyResponse.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        surveyResponseService.saveSurveyResponse(req);

        // Then
        verify(answerResponseValidator).checkResponseDate(survey);
        verify(answerResponseValidator).validateRequiredQuestions(
                argThat(list -> list.size() == 1 && list.get(0).getId().equals(100L)),
                anyMap()
        );
        verify(answerResponseValidator).validateAnswerResponse(ar1, survey.getQuestions());
        verify(answerResponseValidator).validateAnswerResponse(ar2, survey.getQuestions());

        verify(surveyResponseRepository).save(captor.capture());
        SurveyResponse saved = captor.getValue();

        assertSame(survey, saved.getSurvey());
        assertSame(user, saved.getUser());
        assertNotNull(saved.getSubmittedAt());

        assertEquals(1, saved.getAnswerResponses().size());
        assertSame(qReq, saved.getAnswerResponses().get(0).getQuestion());
        assertEquals(List.of("A"), saved.getAnswerResponses().get(0).getAnswerValues());
        assertSame(saved, saved.getAnswerResponses().get(0).getSurveyResponse());
    }

    @Test
    public void shouldThrowWhenUserAlreadyAnswered() {
        // Given
        AppUser user = new AppUser();
        user.setId(1L);
        Survey survey = new Survey();
        survey.setId(2L);

        Question q = new Question();
        q.setId(10L);
        q.setRequired(true);
        q.setAnswers(List.of(new Answers("A", q)));
        survey.setQuestions(List.of(q));

        AnswerResponseRequest ar = mock(AnswerResponseRequest.class);
        when(ar.getQuestionId()).thenReturn(10L);
        when(ar.getAnswerValues()).thenReturn(List.of("A"));

        SurveyResponseRequest req = mock(SurveyResponseRequest.class);
        when(req.getSurveyId()).thenReturn(2L);
        when(req.getUserId()).thenReturn(1L);
        when(req.getAnswerResponses()).thenReturn(List.of(ar));

        when(surveysRepository.findSurveyById(2L)).thenReturn(survey);
        when(userRepository.getAppUserById(1L)).thenReturn(user);

        when(answerResponseValidator.findQuestionById(10L, survey.getQuestions())).thenReturn(q);
        when(surveyResponseRepository.existsBySurveyAndUser(survey, user)).thenReturn(true);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> surveyResponseService.saveSurveyResponse(req));
        assertEquals(ErrorMessages.USER_ALREADY_ANSWERED, ex.getMessage());
        verify(surveyResponseRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenSurveyNotFoundForResults() {
        // Given
        when(surveysRepository.findSurveyById(99L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> surveyResponseService.getSurveyResults(99L));
        assertEquals(ErrorMessages.SURVEY_NOT_FOUND, ex.getMessage());
    }

    @Test
    public void shouldAggregateSurveyResultsWithCounts() {
        // Given
        Survey survey = new Survey();
        survey.setId(7L);
        survey.setTitle("Title");
        survey.setDescription("Desc");
        ZonedDateTime end = ZonedDateTime.now(ZoneOffset.UTC).plusDays(2);
        survey.setEndTime(end);

        Question q1 = new Question();
        q1.setId(100L);
        q1.setValue("Q1");
        Answers q1a1 = new Answers();
        q1a1.setValue("A");
        Answers q1a2 = new Answers();
        q1a2.setValue("B");
        q1.setAnswers(List.of(q1a1, q1a2));

        Question q2 = new Question();
        q2.setId(200L);
        q2.setValue("Q2");
        Answers q2a1 = new Answers();
        q2a1.setValue("X");
        q2.setAnswers(List.of(q2a1));

        survey.setQuestions(List.of(q1, q2));

        when(surveysRepository.findSurveyById(7L)).thenReturn(survey);
        when(surveyResponseRepository.countByQuestionAndAnswer(q1, "A")).thenReturn(3);
        when(surveyResponseRepository.countByQuestionAndAnswer(q1, "B")).thenReturn(1);
        when(surveyResponseRepository.countByQuestionAndAnswer(q2, "X")).thenReturn(5);

        when(surveyResponseRepository.countResponsesBySurveyId(7L)).thenReturn(6);

        // When
        SurveyResults results = surveyResponseService.getSurveyResults(7L);

        // Then
        assertEquals("Title", results.getSurveyTitle());
        assertEquals("Desc", results.getSurveyDescription());
        assertEquals(6, results.getTotalAnswers());
        assertEquals(end, results.getEndTime());

        assertEquals(2, results.getQuestionResults().size());

        SurveyResults.QuestionResult qr1 = results.getQuestionResults().get(0);
        assertEquals("Q1", qr1.getQuestionText());
        assertEquals(2, qr1.getAnswerResults().size());
        assertEquals("A", qr1.getAnswerResults().get(0).getAnswerText());
        assertEquals(3, qr1.getAnswerResults().get(0).getCount());
        assertEquals("B", qr1.getAnswerResults().get(1).getAnswerText());
        assertEquals(1, qr1.getAnswerResults().get(1).getCount());

        SurveyResults.QuestionResult qr2 = results.getQuestionResults().get(1);
        assertEquals("Q2", qr2.getQuestionText());
        assertEquals(1, qr2.getAnswerResults().size());
        assertEquals("X", qr2.getAnswerResults().get(0).getAnswerText());
        assertEquals(5, qr2.getAnswerResults().get(0).getCount());
    }
}
