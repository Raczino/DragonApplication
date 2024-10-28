package com.raczkowski.app.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n from Notification n WHERE n.userId = :id")
    List<Notification> getAllNotificationsForUser(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.timestampRead = :timestamp where n.id = :id ")
    void markNotificationAsRead(@Param("id") Long id, @Param("timestamp") ZonedDateTime timestamp);
}
