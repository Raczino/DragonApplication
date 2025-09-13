package com.raczkowski.app.notification;

import com.raczkowski.app.common.offset.SliceResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@AllArgsConstructor
public class NotificationsController {
    private final NotificationService notificationService;

    @PostMapping("/read")
    void readNotification(@RequestParam Long id) {
        notificationService.markNotificationAsRead(id);
    }

    @GetMapping("/get/user")
    public ResponseEntity<SliceResponse<Notification>> getForUser(
            @RequestParam String id,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(notificationService.getForUserOffset(id, offset, limit));
    }
}
