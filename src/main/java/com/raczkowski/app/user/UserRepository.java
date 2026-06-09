package com.raczkowski.app.user;

import com.raczkowski.app.enums.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

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

    @Query("SELECT uf FROM AppUser u JOIN u.followers uf WHERE u.id = :userId")
    List<AppUser> findFollowersByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT uf FROM AppUser u JOIN u.followers uf
            WHERE u.id = :userId
            """)
    Slice<AppUser> findFollowersSliceByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT uf FROM AppUser u JOIN u.followedUsers uf WHERE u.id = :userId")
    List<AppUser> findFollowingByUserId(@Param("userId") Long userId);

    @Query("""
             SELECT uf FROM AppUser u JOIN u.followedUsers uf
             WHERE u.id = :userId
            """)
    Slice<AppUser> findFollowingSliceByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT CASE WHEN COUNT(fu) > 0 THEN TRUE ELSE FALSE END
            FROM AppUser u
            JOIN u.followedUsers fu
            WHERE u.id = :loggedUserId AND fu.id = :userId
            """)
    boolean findFollowUserById(@Param("loggedUserId") Long loggedUserId,
                               @Param("userId") Long userId);
}
