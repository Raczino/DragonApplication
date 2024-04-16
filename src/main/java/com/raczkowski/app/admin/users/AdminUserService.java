package com.raczkowski.app.admin.users;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminUserService {
    UserRepository userRepository;
    AdminValidator adminValidator;

    public void changeUserPermission(PermissionRequest permissionRequest) { //TODO: Poprawić
        adminValidator.validateIfUserIsAdminOrOperator();
        userRepository.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.userRole);
    }
}
