package com.raczkowski.app.article;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ArticleRequestValidatorTest {

    @Mock
    private FeatureLimitHelperService featureLimitHelperService;

    private ArticleRequestValidator articleRequestValidator;

    private final AppUser user = new AppUser();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        articleRequestValidator = new ArticleRequestValidator(featureLimitHelperService);
    }

    private Limits createValidLimits() {
        return new Limits(
                20,     // articleContentMinLength
                200000, // articleContentMaxLength
                8,      // articleTitleMinLength
                500,    // articleTitleMaxLength
                10,     // hashtagsMaxLength
                10,     // commentContentMinLength
                1000,   // commentContentMaxLength
                99999,  // articleLimit
                99999,  // commentLimit
                20,     // surveyLimit
                10,     // surveyQuestionLimit
                10      // surveyQuestionAnswerLimit
        );
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        // given
        ArticleRequest request = new ArticleRequest(null, "content");
        request.setContentHtml("html");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        ResponseException exception = assertThrows(ResponseException.class,
                () -> articleRequestValidator.validateArticleRequest(request, user));

        assertEquals(ErrorMessages.TITLE_AND_CONTENT_CANNOT_BE_EMPTY, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContentIsNull() {
        // given
        ArticleRequest request = new ArticleRequest("title", null);
        request.setContentHtml("html");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        ResponseException exception = assertThrows(ResponseException.class,
                () -> articleRequestValidator.validateArticleRequest(request, user));

        assertEquals(ErrorMessages.TITLE_AND_CONTENT_CANNOT_BE_EMPTY, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContentHtmlIsNull() {
        // given
        ArticleRequest request = new ArticleRequest("title", "content");
        request.setContentHtml(null);
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        ResponseException exception = assertThrows(ResponseException.class,
                () -> articleRequestValidator.validateArticleRequest(request, user));

        assertEquals(ErrorMessages.TITLE_AND_CONTENT_CANNOT_BE_EMPTY, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContentTooShort() {
        // given
        ArticleRequest request = new ArticleRequest("valid title", "1234");
        request.setContentHtml("html");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        ResponseException exception = assertThrows(ResponseException.class,
                () -> articleRequestValidator.validateArticleRequest(request, user));

        assertEquals(ErrorMessages.CONTENT_IS_TOO_SHORT + " 10", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenScheduledDateIsInPast() {
        // given
        ArticleRequest request = new ArticleRequest("valid title", "valid content");
        request.setContentHtml("html");
        request.setScheduledForDate(java.time.ZonedDateTime.now().minusDays(1));
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        ResponseException exception = assertThrows(ResponseException.class,
                () -> articleRequestValidator.validateArticleRequest(request, user));

        assertEquals(ErrorMessages.SCHEDULED_FOR_MUST_BE_IN_FUTURE, exception.getMessage());
    }

    @Test
    void shouldPassValidationForCorrectRequest() {
        // given
        ArticleRequest request = new ArticleRequest("valid title", "valid content");
        request.setContentHtml("html");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        articleRequestValidator.validateArticleRequest(request, user);
    }

    @Test
    void shouldPassValidationWithMinimumLengthTitleAndContent() {
        // given
        String minTitle = "a".repeat(createValidLimits().getArticleTitleMinLength());
        String minContent = "a".repeat(createValidLimits().getCommentContentMinLength());
        ArticleRequest request = new ArticleRequest(minTitle, minContent);
        request.setContentHtml("<p>HTML content</p>");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        articleRequestValidator.validateArticleRequest(request, user);
    }

    @Test
    void shouldPassValidationWithFutureScheduledDate() {
        // given
        ArticleRequest request = new ArticleRequest("Valid Title", "Valid content with enough length.");
        request.setContentHtml("<p>Valid content HTML</p>");
        request.setScheduledForDate(java.time.ZonedDateTime.now().plusDays(1));
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        articleRequestValidator.validateArticleRequest(request, user);
    }

    @Test
    void shouldPassValidationWithValidHashtags() {
        // given
        ArticleRequest request = new ArticleRequest("Valid Title", "Valid content with enough length.");
        request.setContentHtml("<p>Valid content HTML</p>");
        request.setHashtags("#tag1 #tag2");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        articleRequestValidator.validateArticleRequest(request, user);
    }

    @Test
    void shouldPassValidationWithValidBasicData() {
        // given
        ArticleRequest request = new ArticleRequest("Valid Title", "Valid content with enough length.");
        request.setContentHtml("<p>Valid content HTML</p>");
        when(featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)).thenReturn(true);
        when(featureLimitHelperService.getFeaturesLimits(user.getId())).thenReturn(createValidLimits());

        // when / then
        articleRequestValidator.validateArticleRequest(request, user);
    }
}
