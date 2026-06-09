package com.raczkowski.app.notification;

import com.raczkowski.app.common.offset.OffsetPagination;
import com.raczkowski.app.common.offset.SliceResponse;
import com.raczkowski.app.enums.NotificationType;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public void sendNotification(String userId, Notification notification) {
        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Transactional
    public void markNotificationAsRead(Long id) {
        notificationRepository.markNotificationAsRead(id, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public SliceResponse<Notification> getForUserOffset(String userId, Integer offset, Integer limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));

        return OffsetPagination.fetch(
                offset,
                limit,
                sort,
                20,
                200,
                pageable -> notificationRepository.findForUser(userId, pageable)
        );
    }

    @Transactional
    public void sendNotification(NotificationType type, String userId, AppUser createdBy, String title, String message, String targetUrl) {
        Notification notification = new Notification(
                userId,
                type,
                title,
                message,
                ZonedDateTime.now(ZoneOffset.UTC),
                createdBy.getFirstName(),
                targetUrl
        );
        saveNotification(notification);
        sendNotification(userId, notification);
    }
}
