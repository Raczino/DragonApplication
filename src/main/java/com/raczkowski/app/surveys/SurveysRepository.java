package com.raczkowski.app.surveys;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveysRepository extends JpaRepository<Survey, Long> {
}
