package com.raczkowski.app.surveys.surveyResponse;

import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.Answers;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AnswerResponseValidator {

    public void checkResponseDate(Survey survey) {
        if (survey.getEndTime().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new ResponseException("Survey already ended");
        }
    }

    public void validateRequiredQuestions(List<Question> requiredQuestions, Map<Long, List<String>> providedAnswers) {
        for (Question requiredQuestion : requiredQuestions) {
            if (!providedAnswers.containsKey(requiredQuestion.getId()) ||
                    providedAnswers.get(requiredQuestion.getId()) == null) {
                throw new ResponseException("Question " + requiredQuestion.getId() + " is required.");
            }
        }
    }

    public void validateAnswerResponse(AnswerResponseRequest answerResponseRequest, List<Question> questions) {
        Question question = findQuestionById(answerResponseRequest.getQuestionId(), questions);
        if (question == null) {
            throw new ResponseException("Question not found for ID: " + answerResponseRequest.getQuestionId());
        }

        if (question.isRequired() && (answerResponseRequest.getAnswerValues() == null || answerResponseRequest.getAnswerValues().isEmpty())) {
            throw new ResponseException("Answer is required for this question.");
        }

        if (answerResponseRequest.getAnswerValues() != null) {
            int selectedCount = answerResponseRequest.getAnswerValues().size();
            if (selectedCount < question.getMinSelected() || selectedCount > question.getMaxSelected()) {
                throw new ResponseException("Number of selected answers must be between " + question.getMinSelected() + " and " + question.getMaxSelected());
            }

            validateAnswerValues(answerResponseRequest.getAnswerValues(), question);
        }
    }

    private void validateAnswerValues(List<String> answerValues, Question question) {
        List<String> validAnswers = question.getAnswers().stream()
                .map(Answers::getValue)
                .toList();

        for (String answer : answerValues) {
            if (!validAnswers.contains(answer)) {
                throw new ResponseException("Invalid answer value: " + answer);
            }
        }
    }

    protected Question findQuestionById(Long questionId, List<Question> questions) {
        return questions.stream()
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }
}