package com.raczkowski.app.surveys.surveyResponse;

import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.Answers;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;
import com.raczkowski.app.surveys.survey.SurveysRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SurveyResponseService {

    private final SurveysRepository surveysRepository;
    private final UserRepository userRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final AnswerResponseValidator answerResponseValidator;

    @Transactional
    public void saveSurveyResponse(SurveyResponseRequest surveyResponseRequest) {
        Survey survey = surveysRepository.findSurveyById(surveyResponseRequest.getSurveyId());
        AppUser user = userRepository.getAppUserById(surveyResponseRequest.getUserId());

        if (survey == null || user == null) {
            throw new ResponseException("Survey or user not found");
        }
        answerResponseValidator.checkResponseDate(survey);

        List<Question> requiredQuestions = survey.getQuestions().stream()
                .filter(Question::isRequired)
                .toList();

        Map<Long, List<String>> providedAnswers = surveyResponseRequest.getAnswerResponses().stream()
                .filter(answerRequest -> answerRequest.getAnswerValues() != null)
                .collect(Collectors.toMap(
                        AnswerResponseRequest::getQuestionId,
                        AnswerResponseRequest::getAnswerValues
                ));

        answerResponseValidator.validateRequiredQuestions(requiredQuestions, providedAnswers);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setSurvey(survey);
        surveyResponse.setUser(user);
        surveyResponse.setSubmittedAt(ZonedDateTime.now(ZoneOffset.UTC));

        for (AnswerResponseRequest answerRequest : surveyResponseRequest.getAnswerResponses()) {
            answerResponseValidator.validateAnswerResponse(answerRequest, survey.getQuestions());

            Question question = answerResponseValidator.findQuestionById(answerRequest.getQuestionId(), survey.getQuestions());

            if (surveyResponseRepository.existsBySurveyAndUser(survey, user)) {
                throw new ResponseException("User has already answered this question.");
            }

            if (answerRequest.getAnswerValues() == null && !question.isRequired()) continue;

            AnswerResponse answerResponse = new AnswerResponse();
            answerResponse.setQuestion(question);
            answerResponse.setSurveyResponse(surveyResponse);
            answerResponse.setAnswerValues(answerRequest.getAnswerValues());
            surveyResponse.getAnswerResponses().add(answerResponse);
        }

        surveyResponseRepository.save(surveyResponse);
    }

    public SurveyResults getSurveyResults(Long surveyId) {
        Survey survey = surveysRepository.findSurveyById(surveyId);

        if (survey == null) {
            throw new ResponseException("Survey not found");
        }

        List<SurveyResults.QuestionResult> questionResults = new ArrayList<>();

        for (Question question : survey.getQuestions()) {
            List<SurveyResults.AnswerResult> answerResults = new ArrayList<>();

            for (Answers answer : question.getAnswers()) {
                int count = surveyResponseRepository.countByQuestionAndAnswer(question, answer.getValue());
                answerResults.add(new SurveyResults.AnswerResult(answer.getValue(), count));
            }

            questionResults.add(new SurveyResults.QuestionResult(question.getValue(), answerResults));
        }

        int totalResponses = surveyResponseRepository.countResponsesBySurveyId(survey.getId());

        return new SurveyResults(
                survey.getTitle(),
                survey.getDescription(),
                totalResponses,
                survey.getEndTime(),
                questionResults
        );
    }
}
