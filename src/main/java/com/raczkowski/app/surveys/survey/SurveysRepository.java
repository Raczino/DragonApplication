package com.raczkowski.app.surveys.survey;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.dto.SurveyDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveysRepository extends JpaRepository<Survey, Long> {
    Survey findSurveyById(Long id);

    List<Survey> findAllByOwner(AppUser owner);

    /**
     * Artykuły autorów, których DANY UŻYTKOWNIK obserwuje.
     * (follower = :userId  →  bierzemy followed → ich artykuły)
     */
    @Query("""
                select s
                from Survey s
                where s.owner in (
                    select fu
                    from AppUser u
                    join u.followedUsers fu
                    where u.id = :userId
                )
            """)
    Page<Survey> findSurveyByAuthorsIFollow(@Param("userId") Long userId, Pageable pageable);
}
