package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ModeratorStatisticRepository extends JpaRepository<ModeratorStatistic, Long> {

    ModeratorStatistic getModeratorStatisticByAppUser(AppUser user);

    boolean existsByAppUser(AppUser user);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.approvedArticleCounter = m.approvedArticleCounter + 1 WHERE m.id = :userId")
    void increaseApprovedArticleCount(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.rejectedArticleCounter = m.rejectedArticleCounter + 1 WHERE m.id = :userId")
    void increaseRejectedArticleCount(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.deletedArticleCounter = m.deletedArticleCounter + 1 WHERE m.id = :userId")
    void increaseDeletedArticleCount(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.deletedCommentCounter = m.deletedCommentCounter + 1 WHERE m.id = :userId")
    void increaseDeletedCommentCount(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.deletedSurveyCounter = m.deletedSurveyCounter + 1 WHERE m.id = :userId")
    void increaseDeletedSurveyCount(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE ModeratorStatistic m SET m.pinnedArticleCounter = m.pinnedArticleCounter + 1 WHERE m.id = :userId")
    void increasePinnedArticleCount(@Param("userId") Long userId);
}
