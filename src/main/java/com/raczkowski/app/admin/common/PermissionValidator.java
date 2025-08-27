package com.raczkowski.app.admin.common;

import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class PermissionValidator {
    private final UserService userService;

    public AppUser validateIfUserIsAdminOrModerator() {
        AppUser user = userService.getLoggedUser();
        if (user.getUserRole() == UserRole.ADMIN || user.getUserRole() == UserRole.MODERATOR || user.getUserRole() == UserRole.OPERATOR) {
            return user;
        }
        throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
    }

    public boolean validateAdmin() {
        return userService.getLoggedUser().getUserRole() == UserRole.ADMIN;
    }

    public AppUser validateOperatorOrAdmin() {
        AppUser user = userService.getLoggedUser();
        if (user.getUserRole() == UserRole.OPERATOR || user.getUserRole() == UserRole.ADMIN) {
            return user;
        }
        throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
    }
}
