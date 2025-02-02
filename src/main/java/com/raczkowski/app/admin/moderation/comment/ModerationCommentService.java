package com.raczkowski.app.admin.moderation.comment;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModerationCommentService {
    private final CommentService commentService;
    private final PermissionValidator permissionValidator;

    public void deleteComment(Long commentId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        commentService.removeComment(commentId);
    }
}
