package com.raczkowski.app.admin.moderation.survey;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("webapi/v1/moderator/survey")
@AllArgsConstructor
public class ModerationSurveyController {
    private final ModerationSurveyService moderationSurveyService;

    @PostMapping("survey/delete")
    public void deleteSurvey(@RequestParam Long id) {
        moderationSurveyService.deleteSurvey(id);
    }
}
