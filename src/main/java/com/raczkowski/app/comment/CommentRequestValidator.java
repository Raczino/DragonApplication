package com.raczkowski.app.comment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.exceptions.ResponseException;
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

    private final AdminSettingsService adminSettingsService;

    public void validateCreationRequest(CommentRequest commentRequest) {
        int contentMaxLength = Integer.parseInt(adminSettingsService.getSetting("comment.content.max.length").getSettingValue());

        if (commentRequest.getContent() == null || commentRequest.getContent().equals("")) {
            throw new ResponseException("Content cannot be null");
        } else if (commentRequest.getId() == null) {
            throw new ResponseException("Article id cannot be null");
        }

        validateCommentContentForBannedWords(commentRequest.getContent());

        if (commentRequest.getContent().length() > contentMaxLength) {
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
