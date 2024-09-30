package com.raczkowski.app.surveys;

import com.raczkowski.app.enums.SurveyQuestionType;
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

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answers> answers;

    private int minSelected;

    private int maxSelected;

    public Question(String value, SurveyQuestionType type, boolean required, List<Answers> answers, int minSelected, int maxSelected) {
        this.value = value;
        this.type = type;
        this.required = required;
        this.answers = answers;
        this.minSelected = minSelected;
        this.maxSelected = maxSelected;
    }
}