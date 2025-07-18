package com.raczkowski.app.history;

import com.raczkowski.app.enums.HistoryMethodType;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.postgresql.util.PGobject;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Getter
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private HistoryMethodType methodType;

    @Column(updatable = false)
    @CreationTimestamp
    private ZonedDateTime date;

    @Column(name = "entity_type")
    private String entityType;

    @Column(columnDefinition = "jsonb")
    private PGobject payload;

    public History(AppUser user, Long entityId, HistoryMethodType methodType, String entityType, PGobject payload) {
        this.user = user;
        this.entityId = entityId;
        this.methodType = methodType;
        this.entityType = entityType;
        this.payload = payload;
    }

    public History() {

    }
}
