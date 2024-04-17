package com.raczkowski.app.admin.users;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminUserService {
    UserRepository userRepository;
    AdminValidator adminValidator;

    public void changeUserPermission(PermissionRequest permissionRequest) {
        adminValidator.validateIfUserIsAdminOrOperator();
        if (userRepository.getAppUserById(permissionRequest.getId()) == null) {
            throw new ResponseException("User doesn't exists");
        }
        userRepository.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.userRole);
    }
}
