package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveysRepository extends JpaRepository<Survey, Long> {
    Survey findSurveyById(Long id);

    List<Survey> findAllByOwner(AppUser owner);
}
