package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModeratorStatisticDto {
    private Long id;
    private int approvedArticleCounter;
    private int rejectedArticleCounter;
    private int deletedArticleCounter;
    private int deletedCommentCounter;
    private int deletedSurveyCounter;
    private int editedArticleCounter;
    private int pinnedArticleCounter;
    private AuthorDto moderatorId;
}
