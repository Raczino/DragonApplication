package com.raczkowski.app.notification;

import com.raczkowski.app.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    SimpMessagingTemplate template;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void send_shouldConvertAndSendToUserTopic() {
        // given
        Notification n = new Notification();

        // when
        notificationService.sendNotification("123", n);

        // then
        verify(template).convertAndSend(eq("/topic/notifications/123"), same(n));
    }

    @Test
    void sendShouldUseProperDestinationForAnyUserId() {
        Notification n = new Notification();
        notificationService.sendNotification("999", n);
        verify(template).convertAndSend(eq("/topic/notifications/999"), same(n));
    }

    @Test
    void save_shouldDelegateToRepo() {
        Notification n = new Notification();
        notificationService.saveNotification(n);
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_shouldCallRepoWithNow() {
        notificationService.markNotificationAsRead(42L);
        verify(notificationRepository).markNotificationAsRead(eq(42L), any());
    }

    @Test
    void listForUser_shouldDelegateToRepo() {
        notificationService.getAllNotificationsForUser("7");
        verify(notificationRepository).getAllNotificationsForUser("7");
    }

    @Test
    public void shouldSaveNotification() {
        // Given
        Notification n = new Notification();

        // When
        notificationService.saveNotification(n);

        // Then
        verify(notificationRepository).save(n);
    }

    @Test
    public void shouldSendNotificationToUserTopic() {
        // Given
        Notification n = new Notification("123", NotificationType.ARTICLE_PUBLISH, "title", "msg",
                ZonedDateTime.now(), "me", "/target");

        // When
        notificationService.sendNotification("123", n);

        // Then
        verify(template).convertAndSend(eq("/topic/notifications/123"), same(n));
    }

    @Test
    public void shouldMarkNotificationAsReadWithCurrentUtcTime() {
        // Given
        Long id = 77L;
        ArgumentCaptor<ZonedDateTime> timeCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);

        // When
        notificationService.markNotificationAsRead(id);

        // Then
        verify(notificationRepository).markNotificationAsRead(eq(id), timeCaptor.capture());

        ZonedDateTime used = timeCaptor.getValue();
        assertNotNull(used);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        assertTrue(!used.isBefore(now.minusSeconds(5)) && !used.isAfter(now.plusSeconds(5)));
        assertEquals(ZoneOffset.UTC, used.getOffset());
    }

    @Test
    public void shouldGetAllNotificationsForUser() {
        // Given
        String userId = "42";
        Notification n1 = new Notification();
        Notification n2 = new Notification();
        when(notificationRepository.getAllNotificationsForUser(userId)).thenReturn(List.of(n1, n2));

        // When
        List<Notification> out = notificationService.getAllNotificationsForUser(userId);

        // Then
        assertEquals(2, out.size());
        assertSame(n1, out.get(0));
        assertSame(n2, out.get(1));
        verify(notificationRepository).getAllNotificationsForUser(userId);
    }
}
