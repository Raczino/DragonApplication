package com.raczkowski.app.surveys.answers;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raczkowski.app.surveys.questions.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Answers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference
    private Question question;

    public Answers(String value, Question question) {
        this.value = value;
        this.question = question;
    }
}
