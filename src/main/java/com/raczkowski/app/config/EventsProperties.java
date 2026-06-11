package com.raczkowski.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "events")
public class EventsProperties {

    private String exchange = "dragon.events.x";
    private Retry retry = new Retry();
    private Dedup dedup = new Dedup();

    @Setter
    @Getter
    public static class Retry {
        private int maxAttempts = 5;
        private List<Long> delays = List.of(10000L, 60000L, 300000L);
    }

    @Setter
    @Getter
    public static class Dedup {
        private long ttlSec = 3600;
    }
}