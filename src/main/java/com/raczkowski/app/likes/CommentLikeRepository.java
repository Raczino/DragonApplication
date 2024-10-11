package com.raczkowski.app.likes;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsCommentLikeByAppUserAndComment(AppUser appUser, Comment comment);

    CommentLike findByCommentAndAppUser(Comment comment, AppUser user);

    List<CommentLike> findAllByComment(Comment comment);
    void deleteCommentLikesByComment(Comment comment);
}
