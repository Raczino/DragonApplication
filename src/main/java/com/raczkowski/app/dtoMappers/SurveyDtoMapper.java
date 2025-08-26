package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.AnswersDto;
import com.raczkowski.app.dto.QuestionDto;
import com.raczkowski.app.dto.SurveyDto;
import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class SurveyDtoMapper {

    private final AuthorDtoMapper authorDtoMapper;

    public SurveyDto toDTO(Survey survey) {
        final List<QuestionDto> questionDTOs = mapQuestions(survey);
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .createdAt(survey.getCreatedAt())
                .endTime(survey.getEndTime())
                .scheduledFor(survey.getScheduledFor())
                .author(authorDtoMapper.toAuthorDto(survey.getOwner()))
                .questions(questionDTOs)
                .isActive(survey.getScheduledFor().isBefore(now) && survey.getEndTime().isAfter(now))
                .build();
    }

    private List<QuestionDto> mapQuestions(Survey survey) {
        return survey.getQuestions().stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
    }

    private QuestionDto toQuestionDTO(Question question) {
        if (question == null) {
            return null;
        }

        List<AnswersDto> answersDto = question.getAnswers().stream()
                .map(answer -> AnswersDto.builder()
                        .id(answer.getId())
                        .value(answer.getValue())
                        .build())
                .collect(Collectors.toList());

        return QuestionDto.builder()
                .id(question.getId())
                .value(question.getValue())
                .type(question.getType())
                .required(question.isRequired())
                .minSelected(question.getMinSelected())
                .maxSelected(question.getMaxSelected())
                .answers(answersDto)
                .build();
    }
}