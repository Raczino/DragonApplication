package com.raczkowski.app.article;

import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.exceptions.ResponseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ArticleRequestValidator {

    private final AdminSettingsService adminSettingsService;

    public void validateArticleRequest(ArticleRequest request) {
        int contentMaxLength = Integer.parseInt(adminSettingsService.getSetting("article.content.max.length").getSettingValue());
        int titleMaxLength = Integer.parseInt(adminSettingsService.getSetting("article.title.max.length").getSettingValue());
        int hashtagsMaxLength = Integer.parseInt(adminSettingsService.getSetting("article.hashtag.max.length").getSettingValue());
        int contentMinLength = Integer.parseInt(adminSettingsService.getSetting("article.content.min.length").getSettingValue());
        int titleMinLength = Integer.parseInt(adminSettingsService.getSetting("article.title.min.length").getSettingValue());

        if (request.getTitle() == null || request.getContent() == null || request.getContentHtml() == null || request.getTitle().isEmpty() || request.getContent().isEmpty()) {
            throw new ResponseException("Title or content can't be empty");
        }

        validateLength("Title", request.getTitle().length(), titleMinLength, titleMaxLength);
        validateLength("Content", request.getContent().length(), contentMinLength, contentMaxLength);

        if (request.getHashtags() != null) {
            if (request.getHashtags().length() > hashtagsMaxLength) {
                throw new ResponseException("Hashtags is longer than maximum length " + hashtagsMaxLength);
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
