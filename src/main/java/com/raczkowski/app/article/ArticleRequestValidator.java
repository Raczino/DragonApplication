package com.raczkowski.app.article;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.limits.Limits;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
@AllArgsConstructor
public class ArticleRequestValidator {
    private final FeatureLimitHelperService featureLimitHelperService;

    public void validateArticleRequest(ArticleRequest request, AppUser user) {
        if (!featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK)) {
            throw new ResponseException("You have reached the weekly article limit. If you need more articles buy premium account.");
        }

        Limits limit = featureLimitHelperService.getFeaturesLimits(user.getId());

        if (request.getTitle() == null || request.getContent() == null || request.getContentHtml() == null
                || request.getTitle().isEmpty() || request.getContent().isEmpty()) {
            throw new ResponseException("Title or content can't be empty");
        }

        validateLength("Title", request.getTitle().length(), limit.getArticleTitleMinLength(), limit.getArticleContentMaxLength());
        validateLength("Content", request.getContent().length(), limit.getCommentContentMinLength(), limit.getArticleContentMaxLength());

        if (request.getHashtags() != null) {
            if (request.getHashtags().length() > limit.getArticleContentMaxLength()) {
                throw new ResponseException("Hashtags is longer than maximum length " + limit.getHashtagsMaxLength());
            }
        }

        if (request.getScheduledForDate() != null) {
            if (request.getScheduledForDate().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
                throw new ResponseException("Scheduled for cannot be before now!");
            }
        }
    }

    private void validateLength(String fieldName, int fieldLength, int minLength, int maxLength) {
        if (fieldLength < minLength) {
            throw new ResponseException(fieldName + " is shorter than minimum length " + minLength);
        }
        if (fieldLength > maxLength) {
            throw new ResponseException(fieldName + " is longer than maximum length " + maxLength);
        }
    }
}
