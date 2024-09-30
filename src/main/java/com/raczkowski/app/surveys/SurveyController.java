package com.raczkowski.app.surveys;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/survey")
public class SurveyController {
    private final SurveyService surveyService;

    @PostMapping("/create")
    public void create(){
        surveyService.createNewSurvey();
    }
}
