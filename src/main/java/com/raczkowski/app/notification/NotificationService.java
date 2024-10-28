package com.raczkowski.app.notification;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String userId, Notification notification) {
        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Transactional
    public void markNotificationAsRead(Long id) {
        notificationRepository.markNotificationAsRead(id, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public List<Notification> getAllNotificationsForUser(String id) {
        return notificationRepository.getAllNotificationsForUser(id);
    }
}
