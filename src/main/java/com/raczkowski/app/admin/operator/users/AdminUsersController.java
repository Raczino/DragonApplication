package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.common.pagination.PageResponse;
import com.raczkowski.app.dto.ModeratorStatisticDto;
import com.raczkowski.app.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webapi/v1/users")
@AllArgsConstructor
public class AdminUsersController {
    private final AdminUserService adminUserService;
    private final ModerationStatisticService moderationStatisticService;

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

    @GetMapping("/moderator/stats")
    public ResponseEntity<ModeratorStatisticDto> getStatisticForModerator(@RequestParam Long id) {
        return ResponseEntity.ok(moderationStatisticService.getStatisticsForUser(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(adminUserService.getAllUsers(size, page));
    }
}
