package com.raczkowski.app.admin.users;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webapi/v1/users")
@AllArgsConstructor
public class AdminUsersController {
    private final AdminUserService adminUserService;

    @PostMapping("/change/permission")
    public void changeUserPermission(@RequestBody PermissionRequest permissionRequest) {
        adminUserService.changeUserPermission(permissionRequest);
    }

    @PostMapping("/block")
    public void blockUser(@RequestParam Long id) {
        adminUserService.blockUser(id);
    }

    @PostMapping("/unblock")
    public void unBlockUser(@RequestParam Long id) {
        adminUserService.unBlockUser(id);
    }
}
