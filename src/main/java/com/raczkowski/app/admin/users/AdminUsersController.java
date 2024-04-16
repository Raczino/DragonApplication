package com.raczkowski.app.admin.users;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webapi/v1/users")
@AllArgsConstructor
public class AdminUsersController {
    AdminUserService adminUserService;

    @PostMapping("/change/permission")
    public void changeUserPermission(@RequestBody PermissionRequest permissionRequest) {
        adminUserService.changeUserPermission(permissionRequest);
    }
}
