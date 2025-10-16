package com.raczkowski.app.rabbit.common;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DeduplicateMessagesCache {
    private final StringRedisTemplate redis;

    @Value("${events.dedupliaction.ttlSeconds:86400}")
    long ttlSec;

    public boolean acquire(String key) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSec));
        return Boolean.TRUE.equals(ok);
    }
}
