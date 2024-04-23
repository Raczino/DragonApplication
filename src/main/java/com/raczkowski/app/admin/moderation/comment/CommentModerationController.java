package com.raczkowski.app.admin.moderation.comment;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webapi/v1/comment")
@AllArgsConstructor
public class CommentModerationController {
    ModerationCommentService moderationCommentService;

    @DeleteMapping("/delete")
    public void deleteComment(@RequestParam Long id) {
        moderationCommentService.deleteComment(id);
    }
}
