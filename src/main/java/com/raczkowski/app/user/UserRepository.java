package com.raczkowski.app.user;

import com.raczkowski.app.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);

    AppUser getAppUserById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser c " +
            "SET c.userRole = :userRole " +
            "WHERE c.id = :id")
    void updateAppUserByUserRole(Long id, UserRole userRole);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser c " +
            "SET c.locked = true, c.blockedDate=:blockedDate "+
            "WHERE c.id = :id")
    void blockUser(Long id, ZonedDateTime blockedDate);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser c " +
            "SET c.locked = false , c.blockedDate=null "+
            "WHERE c.id = :id")
    void unBlockUser(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser u " +
            "SET u.articlesCount = u.articlesCount + 1" +
            "WHERE u.id = :id")
    void updateArticlesCount(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser u " +
            "SET u.commentsCount = u.commentsCount + 1" +
            "WHERE u.id = :id")
    void updateCommentsCount(@Param("id") Long id);
}
