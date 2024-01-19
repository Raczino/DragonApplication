package com.raczkowski.app.article;

import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAll();

    Article findArticleById(Long id);

    List<Article> findAllByAppUser(Optional<AppUser> appUser);

    @Transactional
    @Modifying
    @Query("UPDATE Article c " +
            "SET c.likesNumber = c.likesNumber + 1 " +
            "WHERE c.id = :id")
    void updateArticleLikes(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Article c " +
            "SET c.title = :title, c.content = :content, c.updatedAt = :zonedDateTime, c.isUpdated = true WHERE c.id = :id")
    void updateArticle(@Param("id") Long id, @Param("title") String tile, @Param("content") String content, ZonedDateTime zonedDateTime);

}
