package com.raczkowski.app.admin.moderation.comment;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModerationCommentService {
    CommentService commentService;
    PermissionValidator permissionValidator;
    UserService userService;

    public void deleteComment(Long commentId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        commentService.removeComment(commentId);
    }
}
