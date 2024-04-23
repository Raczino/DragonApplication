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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionValidatorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PermissionValidator permissionValidator;

    @Test
    public void shouldValidateIfUserIsAdmin() {
        AppUser adminUser = new AppUser();
        // Given
        adminUser.setUserRole(UserRole.ADMIN);
        when(userService.getLoggedUser()).thenReturn(adminUser);

        // When
        permissionValidator.validateIfUserIsAdminOrOperator();

        // Then: No exception should be thrown
    }

    @Test
    public void shouldValidateIfUserIsModerator() {
        // Given
        AppUser moderatorUser = new AppUser();
        moderatorUser.setUserRole(UserRole.MODERATOR);
        when(userService.getLoggedUser()).thenReturn(moderatorUser);

        // When
        permissionValidator.validateIfUserIsAdminOrOperator();

        // Then: No exception should be thrown
    }

    @Test
    public void shouldThrowExceptionIfUserIsNotAdminOrModerator() {
        // Given
        AppUser regularUser = new AppUser();
        regularUser.setUserRole(UserRole.USER);
        when(userService.getLoggedUser()).thenReturn(regularUser);

        // When & Then: Expecting a ResponseException to be thrown
        assertThrows(ResponseException.class, () -> permissionValidator.validateIfUserIsAdminOrOperator());
    }
}
