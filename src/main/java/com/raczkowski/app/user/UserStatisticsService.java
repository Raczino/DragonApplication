package com.raczkowski.app.user;

import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public int getArticlesCount(AppUser user) {
        return articleRepository.findAllByAppUser(user).size();
    }

    public int getCommentsCount(AppUser user) {
        return commentRepository.findAllByAppUser(user).size();
    }

    public int getFollowersCount(AppUser user) {
        return userRepository.findFollowersByUserId(user.getId()).size();
    }

    public int getFollowingCount(AppUser user) {
        return userRepository.findFollowingByUserId(user.getId()).size();
    }
}
