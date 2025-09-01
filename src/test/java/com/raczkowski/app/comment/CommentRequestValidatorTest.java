package com.raczkowski.app.comment;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentRequestValidatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FeatureLimitHelperService featureLimitHelperService;

    @InjectMocks
    private CommentRequestValidator validator;

    private AppUser user(long id) {
        AppUser u = new AppUser();
        u.setId(id);
        return u;
    }

    private CommentRequest req(Long articleId, String content) {
        CommentRequest r = new CommentRequest();
        r.setId(articleId);
        r.setContent(content);
        return r;
    }

    @Test
    void shouldThrowWhenWeeklyLimitExceeded() {
        // Given
        AppUser u = user(10L);
        when(featureLimitHelperService.canUseFeature(10L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(false);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(1L, "ok content"), u));

        assertEquals(ErrorMessages.LIMIT_REACHED, ex.getMessage());
        verify(featureLimitHelperService).canUseFeature(10L, FeatureKeys.COMMENT_COUNT_PER_WEEK);
        verify(featureLimitHelperService, never()).getFeaturesLimits(anyLong());
    }

    @Test
    void shouldThrowWhenContentIsNull() {
        AppUser u = user(1L);
        when(featureLimitHelperService.canUseFeature(1L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);

        Limits limits = mock(Limits.class);
        when(featureLimitHelperService.getFeaturesLimits(1L)).thenReturn(limits);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(2L, null), u));

        assertEquals(ErrorMessages.COMMENT_CANNOT_BE_NULL, ex.getMessage());
    }

    @Test
    void shouldThrowWhenContentIsEmpty() {
        AppUser u = user(1L);
        when(featureLimitHelperService.canUseFeature(1L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);

        Limits limits = mock(Limits.class);
        when(featureLimitHelperService.getFeaturesLimits(1L)).thenReturn(limits);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(2L, ""), u));

        assertEquals(ErrorMessages.COMMENT_CANNOT_BE_NULL, ex.getMessage());
    }

    @Test
    void shouldThrowWhenArticleIdIsNull() {
        AppUser u = user(1L);
        when(featureLimitHelperService.canUseFeature(1L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);

        Limits limits = mock(Limits.class);
        when(featureLimitHelperService.getFeaturesLimits(1L)).thenReturn(limits);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(null, "whatever"), u));

        assertEquals(ErrorMessages.ARTICLE_CANNOT_BE_NULL, ex.getMessage());
    }

    @Test
    void shouldThrowWhenContentContainsBannedWord() {
        AppUser u = user(1L);
        when(featureLimitHelperService.canUseFeature(1L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);

        Limits limits = mock(Limits.class);
        when(featureLimitHelperService.getFeaturesLimits(1L)).thenReturn(limits);

        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(3L, "This contains BADWORD inside"), u));

        assertEquals(ErrorMessages.COMMENT_CONTAINS_BANNED_WORDS, ex.getMessage());
    }

    @Test
    void shouldThrowWhenContentTooLong() {
        AppUser u = user(1L);
        when(featureLimitHelperService.canUseFeature(1L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);
        Limits limits = mock(Limits.class);
        when(limits.getArticleContentMaxLength()).thenReturn(10);
        when(featureLimitHelperService.getFeaturesLimits(1L)).thenReturn(limits);

        String longContent = "01234567890";
        ResponseException ex = assertThrows(ResponseException.class, () ->
                validator.validateCreationRequest(req(3L, longContent), u));

        assertEquals(ErrorMessages.COMMENT_TOO_LONG, ex.getMessage());
    }

    @Test
    void shouldPassForValidRequest() {
        AppUser u = user(5L);
        when(featureLimitHelperService.canUseFeature(5L, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(true);
        Limits limits = mock(Limits.class);
        when(limits.getArticleContentMaxLength()).thenReturn(1000);
        when(featureLimitHelperService.getFeaturesLimits(5L)).thenReturn(limits);

        assertDoesNotThrow(() ->
                validator.validateCreationRequest(req(42L, "This is clean and short"), u));

        verify(featureLimitHelperService).canUseFeature(5L, FeatureKeys.COMMENT_COUNT_PER_WEEK);
        verify(featureLimitHelperService).getFeaturesLimits(5L);
    }
}
