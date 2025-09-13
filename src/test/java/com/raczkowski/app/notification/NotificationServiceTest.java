package com.raczkowski.app.notification;

import com.raczkowski.app.common.offset.SliceResponse;
import com.raczkowski.app.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate template;

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
    void shouldSaveNotification() {
        // Given
        Notification n = new Notification();

        // When
        notificationService.saveNotification(n);

        // Then
        verify(notificationRepository).save(n);
    }

    @Test
    void shouldSendNotificationToUserTopic() {
        // Given
        Notification n = new Notification("123", NotificationType.ARTICLE_PUBLISH, "title", "msg",
                ZonedDateTime.now(), "me", "/target");

        // When
        notificationService.sendNotification("123", n);

        // Then
        verify(template).convertAndSend(eq("/topic/notifications/123"), same(n));
    }

    @Test
    void shouldMarkNotificationAsReadWithCurrentUtcTime() {
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
    void getForUserOffset_shouldReturnItemsAndComputeNextOffset_whenHasNext() {
        // Given
        String userId = "42";
        int offset = 0;
        int limit = 2;

        Notification n1 = new Notification();
        n1.setId(1L);
        Notification n2 = new Notification();
        n2.setId(2L);

        // Odpowiedź repo: 2 elementy i hasNext = true
        when(notificationRepository.findForUser(eq(userId), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    return new SliceImpl<>(List.of(n1, n2), p, true);
                });

        // When
        SliceResponse<Notification> resp = notificationService.getForUserOffset(userId, offset, limit);

        // Then
        assertNotNull(resp);
        assertEquals(2, resp.items().size());
        assertTrue(resp.hasNext());
        assertEquals(2, resp.nextOffset());

        // sprawdź poprawny Pageable
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findForUser(eq(userId), pageCaptor.capture());
        Pageable used = pageCaptor.getValue();
        assertEquals(offset / limit, used.getPageNumber());
        assertEquals(limit, used.getPageSize());
        // sort: createdAt DESC, id DESC
        Sort.Order first = used.getSort().getOrderFor("createdAt");
        Sort.Order second = used.getSort().getOrderFor("id");
        assertNotNull(first);
        assertEquals(Sort.Direction.DESC, first.getDirection());
        assertNotNull(second);
        assertEquals(Sort.Direction.DESC, second.getDirection());
    }

    @Test
    void getForUserOffset_shouldSetHasNextFalseAndNextOffsetMinusOne_onLastChunk() {
        // Given
        String userId = "42";
        int offset = 20;
        int limit = 20;

        List<Notification> chunk = List.of(); // ostatnia porcja pusta (też możliwe), ale może być i < limit
        when(notificationRepository.findForUser(eq(userId), any(Pageable.class)))
                .thenAnswer(inv -> new SliceImpl<>(chunk, inv.getArgument(1), false));

        // When
        SliceResponse<Notification> resp = notificationService.getForUserOffset(userId, offset, limit);

        // Then
        assertNotNull(resp);
        assertFalse(resp.hasNext());
        assertEquals(-1, resp.nextOffset());
        assertEquals(0, resp.items().size());
    }

    @Test
    void getForUserOffset_shouldCalculatePageFromOffsetAndLimit() {
        // Given
        String userId = "42";
        int offset = 40;
        int limit = 20; // => page = 2

        when(notificationRepository.findForUser(eq(userId), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    // Zwróć slice z dowolnymi danymi; ważny jest Pageable
                    return new SliceImpl<>(List.of(new Notification()), p, true);
                });

        // When
        notificationService.getForUserOffset(userId, offset, limit);

        // Then
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findForUser(eq(userId), cap.capture());
        Pageable used = cap.getValue();
        assertEquals(2, used.getPageNumber());
        assertEquals(20, used.getPageSize());
    }
}