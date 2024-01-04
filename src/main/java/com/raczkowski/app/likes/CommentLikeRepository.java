package com.raczkowski.app.likes;

import com.raczkowski.app.User.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsAllByAppUser(AppUser appUser);
}
