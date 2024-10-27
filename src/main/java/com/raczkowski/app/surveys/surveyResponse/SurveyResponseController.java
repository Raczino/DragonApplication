package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping("/api/v1/survey-response")
@AllArgsConstructor
public class SurveyResponseController {
    private final SurveyResponseService surveyResponseService;

    @PostMapping("/post")
    void createSurveyResponse(@RequestBody SurveyResponseRequest surveyResponseRequest) {
        surveyResponseService.saveSurveyResponse(surveyResponseRequest);
    }

    @GetMapping("/result")
    ResponseEntity<SurveyResults> getSurveyResults(@RequestParam Long id) {
        return ResponseEntity.ok(surveyResponseService.getSurveyResults(id));
    }
}
