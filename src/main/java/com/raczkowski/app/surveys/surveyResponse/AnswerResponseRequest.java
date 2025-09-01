package com.raczkowski.app.surveys.surveyResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponseRequest {
    private Long questionId;
    private List<String> answerValues;
}
