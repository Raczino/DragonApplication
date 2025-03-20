package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.answers.Answers;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SurveyService {
    private final SurveysRepository surveyRepository;
    private final UserService userService;
    private final SurveyRequestValidator surveyRequestValidator;
    private final FeatureLimitHelperService featureLimitHelperService;

    @Transactional
    public Survey createNewSurvey(SurveyRequest surveyRequest) {
        AppUser user = userService.getLoggedUser();
        surveyRequestValidator.validateSurveyRequest(surveyRequest, user);
        Survey survey = new Survey();
        survey.setTitle(survey.getTitle());
        survey.setDescription(survey.getDescription());
        survey.setCreatedAt(ZonedDateTime.now());
        survey.setEndTime(surveyRequest.getEndTime());
        survey.setOwner(user);

        List<Question> questions = surveyRequest.getQuestions().stream()
                .map(questionRequest -> {
                    Question question = new Question();
                    question.setValue(questionRequest.getValue());
                    question.setType(questionRequest.getType());
                    question.setRequired(questionRequest.isRequired());
                    question.setMinSelected(questionRequest.getMinSelected());
                    question.setMaxSelected(questionRequest.getMaxSelected());

                    question.setSurvey(survey);

                    question.setAnswers(
                            questionRequest.getAnswers().stream()
                                    .map(answerRequest -> new Answers(answerRequest.getValue(), question))
                                    .collect(Collectors.toList())
                    );
                    return question;
                }).collect(Collectors.toList());
        survey.setQuestions(questions);
        featureLimitHelperService.incrementFeatureUsage(user.getId(), FeatureKeys.SURVEY_COUNT_PER_WEEK);

        return surveyRepository.save(survey);
    }

    @Transactional
    public void deleteSurvey(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));

        surveyRepository.delete(survey);
    }

    public Survey getSurveyById(Long id) {
        Survey survey = surveyRepository.findSurveyById(id);
        if (survey == null) {
            throw new ResponseException("Survey with provided id doesn't exists");
        }
        return survey;
    }

    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }
}
