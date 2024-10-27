package com.raczkowski.app.surveys.surveyResponse;

import com.raczkowski.app.surveys.questions.Question;
import com.raczkowski.app.surveys.survey.Survey;
import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    @Query("SELECT COUNT(sr) FROM SurveyResponse sr WHERE sr.survey.id = :surveyId")
    int countResponsesBySurveyId(@Param("surveyId") Long surveyId);

    boolean existsBySurveyAndUser(Survey survey, AppUser user);

    @Query("SELECT COUNT(ar) FROM AnswerResponse ar WHERE ar.question = :question AND :answer MEMBER OF ar.answerValues")
    int countByQuestionAndAnswer(@Param("question") Question question, @Param("answer") String answer);
}
