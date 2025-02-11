package com.raczkowski.app.admin.moderation.survey;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.users.ModerationStatisticService;
import com.raczkowski.app.surveys.survey.SurveyService;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModerationSurveyService {
    private final SurveyService surveyService;
    private final PermissionValidator permissionValidator;
    private final ModerationStatisticService moderationStatisticService;

    public void deleteSurvey(Long id) {
        AppUser user = permissionValidator.validateIfUserIsAdminOrOperator();
        surveyService.deleteSurvey(id);
        moderationStatisticService.surveyDeletedCounterIncrease(user.getId());
    }
}
