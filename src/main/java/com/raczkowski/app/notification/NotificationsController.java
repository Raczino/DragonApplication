package com.raczkowski.app.notification;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    ResponseEntity<List<Notification>> getNotificationsForUser(@RequestParam String id) {
        return ResponseEntity.ok(notificationService.getAllNotificationsForUser(id));
    }
}
