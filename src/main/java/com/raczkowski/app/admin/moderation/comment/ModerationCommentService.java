package com.raczkowski.app.admin.moderation.comment;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModerationCommentService {
    private final CommentService commentService;
    private final PermissionValidator permissionValidator;
    private final ModerationStatisticService moderationStatisticService;

    public void deleteComment(Long commentId) {
        AppUser user = permissionValidator.validateIfUserIsAdminOrOperator();
        commentService.removeComment(commentId);
        moderationStatisticService.commentDeletedCounterIncrease(user.getId());
    }
}
