package com.raczkowski.app.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public int getIntValue(String key, int defaultValue) {
        var v = redisTemplate.opsForValue().get(key);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void setValue(String key, String value, long duration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, duration, timeUnit);
    }

    public void setIntValue(String key, int value, long duration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), duration, timeUnit);
    }

    public void increment(String key, int amount) {
        redisTemplate.opsForValue().increment(key, amount);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public void setTTL(String key, long duration, TimeUnit timeUnit) {
        redisTemplate.expire(key, duration, timeUnit);
    }
}
