package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.dto.ModeratorStatisticDto;
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
}
