package com.raczkowski.app.admin.users;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class AdminUserService {
    private final PermissionValidator permissionValidator;
    private final ModerationStatisticService moderatorStatisticService;
    private final UserService userService;

    public void changeUserPermission(PermissionRequest permissionRequest) {
        boolean isUserAdmin = permissionValidator.validateIfUserIaAdmin();
        UserRole userRole = invokeUserRole(permissionRequest.getId());

        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN) || userRole.equals(UserRole.MODERATOR)) {
                throw new ResponseException("You don't have permission");
            } else {
                userService.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.getUserRole());
            }
        } else {
            userService.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.getUserRole());
        }
        if (permissionRequest.getUserRole().equals(UserRole.MODERATOR)) {
            moderatorStatisticService.createStatisticForUser(permissionRequest.getId());
        }
    }

    public void blockUser(Long id) {
        boolean isUserAdmin = permissionValidator.validateIfUserIaAdmin();
        UserRole userRole = invokeUserRole(id);
        if (userService.getUserById(id) == null) {
            throw new ResponseException("User doesn't exists");
        }
        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN)) {
                throw new ResponseException("You don't have permission to block admin");
            } else {
                userService.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
            }
        } else {
            userService.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
        }
    }

    public void unBlockUser(Long id) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        if (userService.getUserById(id) == null) {
            throw new ResponseException("User doesn't exists");
        }
        userService.unblockUser(id);
    }

    private UserRole invokeUserRole(Long id) {
        AppUser user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseException("User doesn't exists");
        }
        return user.getUserRole();
    }
}
