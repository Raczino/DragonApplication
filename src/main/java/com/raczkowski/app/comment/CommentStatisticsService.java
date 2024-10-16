package com.raczkowski.app.comment;

import com.raczkowski.app.likes.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentStatisticsService {
    private final LikesService likesService;

    public int getLikesCountForComment(Comment comment) {
        return likesService.getLikesCountForComment(comment);
    }
}
