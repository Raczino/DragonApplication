package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @NonNull Page<Article> findAll(@NonNull Pageable pageable);

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
            "SET c.isPinned = true WHERE c.id = :id")
    void pinArticle(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Article a SET a.likesCount = a.likesCount + :likesNumber  WHERE a.id = :id")
    void updateArticleLikesCount(@Param("id") Long id, @Param("likesNumber") int likesNumber);
}
