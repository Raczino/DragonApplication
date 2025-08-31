package com.raczkowski.app.admin.moderation.comment;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModerationCommentServiceTest {

    @Mock
    private CommentService commentService;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private ModerationStatisticService moderationStatisticService;

    @InjectMocks
    private ModerationCommentService moderationCommentService;

    @Test
    public void shouldDeleteCommentAndIncreaseStats() {
        // Given
        AppUser moderator = new AppUser();
        moderator.setId(10L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(moderator);

        when(commentService.removeComment(5L)).thenReturn("Removed");

        // When
        moderationCommentService.deleteComment(5L);

        // Then
        verify(commentService).removeComment(5L);
        verify(moderationStatisticService).commentDeletedCounterIncrease(10L);
    }

    @Test
    public void shouldNotDeleteWhenPermissionDenied() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator())
                .thenThrow(new ResponseException(ErrorMessages.NO_PERMISSION));

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationCommentService.deleteComment(7L));
        assertEquals(ErrorMessages.NO_PERMISSION, ex.getMessage());

        verifyNoInteractions(commentService, moderationStatisticService);
    }

    @Test
    public void shouldPropagateWhenCommentNotFound() {
        // Given
        AppUser mod = new AppUser();
        mod.setId(22L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(mod);

        doThrow(new ResponseException(ErrorMessages.COMMENT_NOT_EXISTS))
                .when(commentService).removeComment(77L);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationCommentService.deleteComment(77L));
        assertEquals(ErrorMessages.COMMENT_NOT_EXISTS, ex.getMessage());

        verify(commentService).removeComment(77L);
        verify(moderationStatisticService, never()).commentDeletedCounterIncrease(anyLong());
    }

    @Test
    public void shouldPropagateWhenUserHasNoRightToRemove() {
        // Given
        AppUser mod = new AppUser();
        mod.setId(33L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(mod);

        doThrow(new ResponseException(ErrorMessages.WRONG_PERMISSION))
                .when(commentService).removeComment(88L);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationCommentService.deleteComment(88L));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());

        verify(commentService).removeComment(88L);
        verify(moderationStatisticService, never()).commentDeletedCounterIncrease(anyLong());
    }
}

