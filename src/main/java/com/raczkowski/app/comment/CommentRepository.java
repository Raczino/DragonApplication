package com.raczkowski.app.comment;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT a FROM Comment a WHERE a.article.id = :articleId ORDER BY a.isPinned DESC, a.likesCount DESC")
    Page<Comment> findCommentsByArticleWithPinnedFirst(@Param("articleId") Long articleId, Pageable pageable);

    Comment findCommentById(Long id);

    Page<Comment> getCommentsByAppUser(AppUser user, Pageable pageable);

    List<Comment> findAllByAppUser(AppUser appUser);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c " +
            "SET c.content = :content, c.updatedAt = :zonedDateTime, c.isUpdated = true WHERE c.id = :id")
    void updateCommentContent(@Param("id") Long id, @Param("content") String content, ZonedDateTime zonedDateTime);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.isPinned = true WHERE c.id = :id")
    void pinComment(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + :likesValue WHERE c.id = :id")
    void updateCommentLikesCount(@Param("id") Long id, @Param("likesValue") int likesValue);
}
