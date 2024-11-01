package com.raczkowski.app.notification;

import com.raczkowski.app.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead = false;
    private ZonedDateTime createdAt;
    private ZonedDateTime timestampRead;
    private String createdBy;
    private String targetUrl;

    public Notification() {
    }

    public Notification(String userId,
                        NotificationType type,
                        String title,
                        String message,
                        ZonedDateTime createdAt,
                        String createdBy,
                        String targetUrl) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.targetUrl = targetUrl;
    }
}
