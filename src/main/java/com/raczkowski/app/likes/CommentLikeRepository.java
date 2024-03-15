package com.raczkowski.app.likes;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsCommentLikeByAppUserAndComment(AppUser appUser, Comment comment);

    CommentLike findByCommentAndAppUser(Comment comment, AppUser user);

    void deleteCommentLikesByComment(Comment comment);
}
