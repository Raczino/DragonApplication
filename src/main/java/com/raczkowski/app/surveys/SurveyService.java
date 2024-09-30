package com.raczkowski.app.surveys;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SurveyService {
    private SurveysRepository surveyRepository;

    public void createNewSurvey() {
    }

}
