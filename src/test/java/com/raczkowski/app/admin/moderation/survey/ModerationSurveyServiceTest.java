package com.raczkowski.app.admin.moderation.survey;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.surveys.survey.SurveyService;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModerationSurveyServiceTest {

    @Mock
    private SurveyService surveyService;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private ModerationStatisticService moderationStatisticService;

    @InjectMocks
    private ModerationSurveyService moderationSurveyService;

    @Test
    void shouldDeleteSurveyAndIncreaseStats() {
        // Given
        AppUser admin = new AppUser();
        admin.setId(10L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(admin);

        // When
        moderationSurveyService.deleteSurvey(5L);

        // Then
        verify(surveyService).deleteSurvey(5L);
        verify(moderationStatisticService).surveyDeletedCounterIncrease(10L);
    }

    @Test
    void shouldNotDeleteSurveyWhenPermissionDenied() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator())
                .thenThrow(new ResponseException(ErrorMessages.NO_PERMISSION));

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationSurveyService.deleteSurvey(77L));

        assertEquals(ErrorMessages.NO_PERMISSION, ex.getMessage());
        verifyNoInteractions(surveyService, moderationStatisticService);
    }

    @Test
    void shouldPropagateExceptionWhenSurveyNotFound() {
        // Given
        AppUser moderator = new AppUser();
        moderator.setId(15L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(moderator);

        doThrow(new ResponseException(ErrorMessages.SURVEY_NOT_FOUND))
                .when(surveyService).deleteSurvey(123L);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationSurveyService.deleteSurvey(123L));

        assertEquals(ErrorMessages.SURVEY_NOT_FOUND, ex.getMessage());
        verify(surveyService).deleteSurvey(123L);
        verify(moderationStatisticService, never()).surveyDeletedCounterIncrease(anyLong());
    }
}