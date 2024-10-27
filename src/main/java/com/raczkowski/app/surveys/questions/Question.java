package com.raczkowski.app.surveys.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.raczkowski.app.enums.SurveyQuestionType;
import com.raczkowski.app.surveys.answers.Answers;
import com.raczkowski.app.surveys.survey.Survey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    private SurveyQuestionType type;

    private boolean required;

    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    @JsonIgnore
    private Survey survey;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Answers> answers;

    private int minSelected;

    private int maxSelected;

    public Question(Long id) {
        this.id = id;
    }
    public Question(String value, SurveyQuestionType type, boolean required, List<Answers> answers, Survey survey, int minSelected, int maxSelected) {
        this.value = value;
        this.type = type;
        this.required = required;
        this.answers = answers;
        this.survey = survey;
        this.minSelected = minSelected;
        this.maxSelected = maxSelected;
    }
}