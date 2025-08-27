package com.raczkowski.app.admin;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionValidatorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PermissionValidator permissionValidator;

    @Test
    public void shouldValidateIfUserIsAdmin() {
        // Given
        AppUser adminUser = new AppUser();
        adminUser.setUserRole(UserRole.ADMIN);
        when(userService.getLoggedUser()).thenReturn(adminUser);

        // When
        AppUser result = permissionValidator.validateIfUserIsAdminOrModerator();

        // then
        assertSame(adminUser, result);
    }

    @Test
    public void shouldValidateIfUserIsModerator() {
        // Given
        AppUser moderatorUser = new AppUser();
        moderatorUser.setUserRole(UserRole.MODERATOR);
        when(userService.getLoggedUser()).thenReturn(moderatorUser);

        // When
        AppUser result = permissionValidator.validateIfUserIsAdminOrModerator();

        // Then:
        assertSame(moderatorUser, result);
    }

    @Test
    public void shouldThrowExceptionIfUserIsNotAdminOrModerator() {
        // Given
        AppUser regularUser = new AppUser();
        regularUser.setUserRole(UserRole.USER);
        when(userService.getLoggedUser()).thenReturn(regularUser);

        // When & Then: Expecting a ResponseException to be thrown
        ResponseException exception = assertThrows(ResponseException.class, () -> permissionValidator.validateIfUserIsAdminOrModerator());
        assertEquals("You don't have permissions to do this action", exception.getMessage());
    }

    @Test
    void ShouldThrowExceptionWhenUserIsNotOperatorOrAdmin() {
        //Given:
        AppUser user = new AppUser();
        user.setUserRole(UserRole.USER);
        when(userService.getLoggedUser()).thenReturn(user);

        //When & Then:
        ResponseException exception = assertThrows(ResponseException.class, () -> permissionValidator.validateOperatorOrAdmin());
        assertEquals("You don't have permissions to do this action", exception.getMessage());
    }

    @Test
    void ShouldReturnTrueWhenUserIsOperatorOrAdmin() {
        //Given:
        AppUser admin = new AppUser();
        AppUser operator = new AppUser();
        admin.setUserRole(UserRole.ADMIN);
        operator.setUserRole(UserRole.ADMIN);
        when(userService.getLoggedUser()).thenReturn(admin, operator);

        // When
        AppUser first = permissionValidator.validateOperatorOrAdmin();
        AppUser second = permissionValidator.validateOperatorOrAdmin();

        // Then
        assertSame(admin, first);
        assertSame(operator, second);
    }
}
