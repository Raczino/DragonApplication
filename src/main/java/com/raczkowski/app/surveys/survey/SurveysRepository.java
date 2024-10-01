package com.raczkowski.app.surveys.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveysRepository extends JpaRepository<Survey, Long> {
    Survey findSurveyById(Long id);
}
