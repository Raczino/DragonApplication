package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    private ZonedDateTime createdAt;

    private ZonedDateTime endTime;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser owner;

    public Survey(List<Question> questions, ZonedDateTime createdAt, ZonedDateTime endTime, AppUser owner) {
        this.questions = questions;
        this.createdAt = createdAt;
        this.endTime = endTime;
        this.owner = owner;
    }
}