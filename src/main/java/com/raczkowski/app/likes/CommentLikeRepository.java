package com.raczkowski.app.likes;

import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsCommentLikeByAppUserAndComment(AppUser appUser, Optional<Comment> comment);
}
