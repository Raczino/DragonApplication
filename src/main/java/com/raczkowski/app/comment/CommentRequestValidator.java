package com.raczkowski.app.comment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.accountPremium.FeatureLimitHelperService;
import com.raczkowski.app.article.Limits;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class CommentRequestValidator {
    private static final String BANNED_WORDS_FILE_PATH = "/bannedWords.json";
    private final List<String> BANNED_WORDS = loadBannedWords();

    private final FeatureLimitHelperService featureLimitHelperService;

    public void validateCreationRequest(CommentRequest commentRequest, AppUser user) {
        Limits limit = featureLimitHelperService.getFeaturesLimits(user.getId());

        if (featureLimitHelperService.canUseFeature(user.getId(), FeatureKeys.COMMENT_COUNT_PER_WEEK)) {
            throw new ResponseException("You have reached the weekly comment limit.");
        }

        if (commentRequest.getContent() == null || commentRequest.getContent().equals("")) {
            throw new ResponseException("Content cannot be null");
        } else if (commentRequest.getId() == null) {
            throw new ResponseException("Article id cannot be null");
        }

        validateCommentContentForBannedWords(commentRequest.getContent());

        if (commentRequest.getContent().length() > limit.getArticleContentMaxLength()) {
            throw new ResponseException("Comment content length is longer than 1000 characters");
        }
    }

    private void validateCommentContentForBannedWords(String content) {
        for (String word : BANNED_WORDS) {
            if (content.toLowerCase().contains(word.toLowerCase())) {
                throw new ResponseException("Comment contains banned words");
            }
        }
    }

    private List<String> loadBannedWords() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<String>> jsonMap = objectMapper.readValue(
                    new ClassPathResource(BANNED_WORDS_FILE_PATH).getInputStream(),
                    new TypeReference<>() {
                    }
            );
            return jsonMap.getOrDefault("banned_words", List.of());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load banned words", e);
        }
    }
}
