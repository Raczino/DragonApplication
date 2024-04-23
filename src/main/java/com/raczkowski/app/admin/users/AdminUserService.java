package com.raczkowski.app.admin.users;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class AdminUserService {
    UserRepository userRepository;
    PermissionValidator permissionValidator;
    ArticleRepository articleRepository;
    UserService userService;

    public void changeUserPermission(PermissionRequest permissionRequest) {
        boolean isUserAdmin = permissionValidator.validateIfUserIaAdmin();
        UserRole userRole = invokeUserRole(permissionRequest.getId());

        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN) || userRole.equals(UserRole.MODERATOR)) {
                throw new ResponseException("You don't have permission");
            } else {
                userRepository.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.userRole);
            }
        } else {
            userRepository.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.userRole);
        }
    }

    public void blockUser(Long id) {
        boolean isUserAdmin = permissionValidator.validateIfUserIaAdmin();
        UserRole userRole = invokeUserRole(id);
        if (userRepository.getAppUserById(id) == null) {
            throw new ResponseException("User doesn't exists");
        }
        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN)) {
                throw new ResponseException("You don't have permission to block admin");
            } else {
                userRepository.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
            }
        }
        userRepository.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public void unBlockUser(Long id) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        if (userRepository.getAppUserById(id) == null) {
            throw new ResponseException("User doesn't exists");
        }
        userRepository.unBlockUser(id);
    }

    private UserRole invokeUserRole(Long id) {
        AppUser user = userRepository.getAppUserById(id);
        UserRole userRole;
        if (user == null) {
            throw new ResponseException("User doesn't exists");
        } else {
            userRole = user.getUserRole();
        }
        return userRole;
    }
}
