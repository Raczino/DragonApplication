package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.AnswersDto;
import com.raczkowski.app.dto.AuthorDto;
import com.raczkowski.app.dto.QuestionDto;
import com.raczkowski.app.dto.SurveyDto;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;

import java.util.List;
import java.util.stream.Collectors;

public class SurveyDtoMapper {
    public static SurveyDto toDTO(Survey survey) {
        if (survey == null) {
            return null;
        }

        List<QuestionDto> questionDTOs = survey.getQuestions().stream()
                .map(SurveyDtoMapper::toQuestionDTO)
                .collect(Collectors.toList());
        AuthorDto authorDto = new AuthorDto(
                survey.getOwner().getId(),
                survey.getOwner().getFirstName(),
                survey.getOwner().getLastName(),
                survey.getOwner().getEmail(),
                survey.getOwner().isAccountBlocked());
        SurveyDto surveyDto = new SurveyDto();
        surveyDto.setId(survey.getId());
        surveyDto.setTitle(survey.getTitle());
        surveyDto.setDescription(survey.getDescription());
        surveyDto.setCreatedAt(survey.getCreatedAt());
        surveyDto.setEndTime(survey.getEndTime());
        surveyDto.setAuthor(authorDto);
        surveyDto.setQuestions(questionDTOs);

        return surveyDto;
    }

    private static QuestionDto toQuestionDTO(Question question) {
        if (question == null) {
            return null;
        }

        return new QuestionDto(
                question.getId(),
                question.getValue(),
                question.getType(),
                question.isRequired(),
                question.getMinSelected(),
                question.getMaxSelected(),
                question.getAnswers().stream()
                        .map(answer -> new AnswersDto(answer.getId(), answer.getValue()))
                        .collect(Collectors.toList())
        );
    }
}