package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private PermissionValidator permissionValidator;
    @Mock
    private ModerationStatisticService moderationStatisticService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    public void shouldChangePermissionAsAdminAndCreateStatsWhenNewRoleIsModerator() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        PermissionRequest req = mock(PermissionRequest.class);
        when(req.getId()).thenReturn(10L);
        when(req.getUserRole()).thenReturn(UserRole.MODERATOR);

        AppUser target = new AppUser();
        target.setId(10L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(10L)).thenReturn(target);

        // When
        adminUserService.changeUserPermission(req);

        // Then
        verify(userService).updateAppUserByUserRole(10L, UserRole.MODERATOR);
        verify(moderationStatisticService).createStatisticForUser(10L);
    }

    @Test
    public void shouldChangePermissionAsAdminAndNotCreateStatsWhenNewRoleIsNotModerator() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        PermissionRequest req = mock(PermissionRequest.class);
        when(req.getId()).thenReturn(11L);
        when(req.getUserRole()).thenReturn(UserRole.ADMIN);

        AppUser target = new AppUser();
        target.setId(11L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(11L)).thenReturn(target);

        // When
        adminUserService.changeUserPermission(req);

        // Then
        verify(userService).updateAppUserByUserRole(11L, UserRole.ADMIN);
        verify(moderationStatisticService, never()).createStatisticForUser(anyLong());
    }

    @Test
    public void shouldThrowWhenNonAdminTriesChangePermissionOfAdminOrModerator() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);

        PermissionRequest req = mock(PermissionRequest.class);
        when(req.getId()).thenReturn(12L);

        AppUser target = new AppUser();
        target.setId(12L);
        target.setUserRole(UserRole.ADMIN);
        when(userService.getUserById(12L)).thenReturn(target);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.changeUserPermission(req));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());

        verify(userService, never()).updateAppUserByUserRole(anyLong(), any());
        verify(moderationStatisticService, never()).createStatisticForUser(anyLong());
        verify(req, never()).getUserRole();
    }

    @Test
    public void shouldChangePermissionAsNonAdminWhenTargetIsNotPrivilegedAndCreateStatsIfBecomesModerator() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);

        PermissionRequest req = mock(PermissionRequest.class);
        when(req.getId()).thenReturn(13L);
        when(req.getUserRole()).thenReturn(UserRole.MODERATOR);

        AppUser target = new AppUser();
        target.setId(13L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(13L)).thenReturn(target);

        // When
        adminUserService.changeUserPermission(req);

        // Then
        verify(userService).updateAppUserByUserRole(13L, UserRole.MODERATOR);
        verify(moderationStatisticService).createStatisticForUser(13L);
    }

    @Test
    public void shouldThrowWhenChangePermissionForNonExistingUser() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        PermissionRequest req = mock(PermissionRequest.class);
        when(req.getId()).thenReturn(99L);

        when(userService.getUserById(99L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.changeUserPermission(req));
        assertEquals(ErrorMessages.USER_NOT_EXITS, ex.getMessage());

        verify(userService, never()).updateAppUserByUserRole(anyLong(), any());
        verify(moderationStatisticService, never()).createStatisticForUser(anyLong());
        verify(req, never()).getUserRole();
    }

    @Test
    public void shouldBlockUserWhenAdmin() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        AppUser target = new AppUser();
        target.setId(20L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(20L)).thenReturn(target);

        // When
        adminUserService.blockUser(20L);

        // Then
        verify(userService).blockUser(eq(20L), any(ZonedDateTime.class));
    }

    @Test
    public void shouldBlockUserWhenNonAdminAndTargetIsNotAdmin() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);

        AppUser target = new AppUser();
        target.setId(21L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(21L)).thenReturn(target);

        // When
        adminUserService.blockUser(21L);

        // Then
        verify(userService).blockUser(eq(21L), any(ZonedDateTime.class));
    }

    @Test
    public void shouldThrowWhenNonAdminBlocksAdmin() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);

        AppUser target = new AppUser();
        target.setId(22L);
        target.setUserRole(UserRole.ADMIN);
        when(userService.getUserById(22L)).thenReturn(target);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.blockUser(22L));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());

        verify(userService, never()).blockUser(anyLong(), any());
    }

    @Test
    public void shouldThrowWhenBlockNonExistingUser() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);
        when(userService.getUserById(23L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.blockUser(23L));
        assertEquals(ErrorMessages.USER_NOT_EXITS, ex.getMessage());

        verify(userService, never()).blockUser(anyLong(), any());
    }

    @Test
    public void shouldUnblockUserWhenHasPermission() {
        // Given
        AppUser moderator = new AppUser();
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(moderator);

        AppUser target = new AppUser();
        target.setId(30L);
        target.setUserRole(UserRole.USER);
        when(userService.getUserById(30L)).thenReturn(target);

        // When
        adminUserService.unBlockUser(30L);

        // Then
        verify(userService).unblockUser(30L);
    }

    @Test
    public void shouldThrowWhenUnblockUserWithoutPermission() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator())
                .thenThrow(new ResponseException(ErrorMessages.WRONG_PERMISSION));

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.unBlockUser(40L));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());

        verify(userService, never()).unblockUser(anyLong());
    }

    @Test
    public void shouldThrowWhenUnblockNonExistingUser() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());
        when(userService.getUserById(41L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminUserService.unBlockUser(41L));
        assertEquals(ErrorMessages.USER_NOT_EXITS, ex.getMessage());

        verify(userService, never()).unblockUser(anyLong());
    }
}
