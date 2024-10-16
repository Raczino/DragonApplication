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

    @Query("SELECT a FROM Comment a ORDER BY a.isPinned DESC")
    Page<Comment> findAllWithPinnedFirst(Pageable pageable);

    Comment findCommentById(Long id);

    List<Comment> getCommentsByArticle(Article article);

    List<Comment> getCommentsByAppUser(AppUser user);

    List<Comment> findAllByAppUser(AppUser appUser);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c " +
            "SET c.content = :content, c.updatedAt = :zonedDateTime, c.isUpdated = true WHERE c.id = :id")
    void updateCommentContent(@Param("id") Long id, @Param("content") String content, ZonedDateTime zonedDateTime);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c " +
            "SET c.isPinned = true WHERE c.id = :id")
    void pinComment(@Param("id") Long id);
}
