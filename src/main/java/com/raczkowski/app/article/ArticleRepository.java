package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @NonNull Page<Article> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = "appUser")
    Page<Article> findAll(@Nullable Specification<Article> spec, Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.isPinned DESC")
    Page<Article> findAllWithPinnedFirst(Pageable pageable);

    Article findArticleById(Long id);

    List<Article> findAllByAppUser(AppUser appUser);

    Page<Article> getArticleByAcceptedBy(AppUser appUser, Pageable pageable);

    List<Article> getAllByStatus(ArticleStatus status);

    @Transactional
    void deleteArticleById(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Article c SET c.status = 'APPROVED' WHERE c.id = :id")
    void updateArticleStatus(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Article c " +
            "SET c.title = :title, c.content = :content, c.updatedAt = :zonedDateTime, c.isUpdated = true WHERE c.id = :id")
    void updateArticle(@Param("id") Long id, @Param("title") String tile, @Param("content") String content, ZonedDateTime zonedDateTime);

    @Transactional
    @Modifying
    @Query("UPDATE Article c " +
            "SET c.isPinned = true, c.pinnedBy = :user WHERE c.id = :id")
    void pinArticle(@Param("id") Long id, AppUser user);

    @Transactional
    @Modifying
    @Query("UPDATE Article a SET a.likesCount = a.likesCount + :likesNumber  WHERE a.id = :id")
    void updateArticleLikesCount(@Param("id") Long id, @Param("likesNumber") int likesNumber);

    /**
     * Artykuły autorów, których DANY UŻYTKOWNIK obserwuje.
     * (follower = :userId  →  bierzemy followed → ich artykuły)
     */
    @Query("""
        select a
        from Article a
        where a.appUser in (
            select fu
            from AppUser u
            join u.followedUsers fu
            where u.id = :userId
        )
    """)
    Page<Article> findArticlesByAuthorsIFollow(@Param("userId") Long userId, Pageable pageable);
}
