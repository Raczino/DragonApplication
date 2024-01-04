package com.raczkowski.app.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAll();

    @Transactional
    @Modifying
    @Query("UPDATE Comment c " +
            "SET c.likesNumber = c.likesNumber + 1 " +
            "WHERE c.id = ?1")
    void updateComment(Long id);
}
