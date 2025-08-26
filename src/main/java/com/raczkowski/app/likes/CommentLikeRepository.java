package com.raczkowski.app.likes;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.user.AppUser;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsCommentLikeByAppUserAndComment(AppUser appUser, Comment comment);

    CommentLike findByCommentAndAppUser(Comment comment, AppUser user);

    List<CommentLike> findAllByComment(Comment comment);

    @Query("SELECT al.comment.id FROM CommentLike al WHERE al.appUser = :user AND al.comment.id IN :commentsIds")
    Set<Long> findLikedCommentIdsByUserAndCommentIds(@Param("user") AppUser user, @Param("articleIds") List<Long> commentsIds);
}
