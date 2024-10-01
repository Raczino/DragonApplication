package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.dto.SurveyDto;
import com.raczkowski.app.dtoMappers.SurveyDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    @PostMapping("/create")
    ResponseEntity<SurveyDto> create(@RequestBody SurveyRequest surveyRequest){
        return ResponseEntity.ok(SurveyDtoMapper.toDTO(surveyService.createNewSurvey(surveyRequest)));
    }

    @GetMapping("/get")
    ResponseEntity<SurveyDto> getSurvey(@RequestParam Long id){
        return ResponseEntity.ok(SurveyDtoMapper.toDTO(surveyService.getSurveyById(id)));
    }
}
