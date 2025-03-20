package com.raczkowski.app.limits;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Limits {
    private final int ArticleContentMinLength;
    private final int ArticleContentMaxLength;
    private final int articleTitleMinLength;
    private final int articleTitleMaxLength;
    private final int hashtagsMaxLength;
    private final int commentContentMinLength;
    private final int commentContentMaxLength;
    private final int articleLimit;
    private final int commentLimit;
    private final int surveyLimit;
    private final int surveyQuestionLimit;
    private final int surveyQuestionAnswerLimit;
}
