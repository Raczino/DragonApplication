package com.raczkowski.app.user;

import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.comment.CommentService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {
    private final ArticleService articleService;
    private final CommentService commentService;
    private final UserService userService;

    public int getArticlesCount(AppUser user) {
        return articleService.getArticlesCountForUser(user);
    }

    public int getCommentsCount(AppUser user) {
        return commentService.getCommentsCountForUser(user);
    }

    public int getFollowersCount(AppUser user) {
        return userService.userFollowersCount(user);
    }

    public int getFollowingCount(AppUser user) {
        return userService.userFollowingCount(user);
    }
}
