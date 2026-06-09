package com.raczkowski.app.admin.moderation.comment;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/moderator/comment")
@AllArgsConstructor
public class CommentModerationController {
    private final ModerationCommentService moderationCommentService;

    @DeleteMapping("/delete")
    public void deleteComment(@RequestParam Long id) {
        moderationCommentService.deleteComment(id);
    }
}
