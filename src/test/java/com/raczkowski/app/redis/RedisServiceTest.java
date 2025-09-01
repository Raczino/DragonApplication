package com.raczkowski.app.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RedisService redisService;

    @Test
    public void shouldGetValue() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn("v");

        // When
        String out = redisService.getValue("k");

        // Then
        assertEquals("v", out);
        verify(valueOps).get("k");
    }

    @Test
    public void shouldGetIntValueWhenPresent() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("int.k")).thenReturn("42");

        // When
        int out = redisService.getIntValue("int.k", -1);

        // Then
        assertEquals(42, out);
        verify(valueOps).get("int.k");
    }

    @Test
    public void shouldReturnDefaultWhenIntValueMissing() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("missing.k")).thenReturn(null);

        // When
        int out = redisService.getIntValue("missing.k", 7);

        // Then
        assertEquals(7, out);
        verify(valueOps).get("missing.k");
    }

    @Test
    public void shouldSetStringValueWithTTL() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        redisService.setValue("k", "v", 1L, TimeUnit.DAYS);

        // Then
        verify(valueOps).set("k", "v", 1L, TimeUnit.DAYS);
    }

    @Test
    public void shouldSetIntValueWithTTL() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        redisService.setIntValue("ik", 5, 30L, TimeUnit.MINUTES);

        // Then
        verify(valueOps).set("ik", "5", 30L, TimeUnit.MINUTES);
    }

    @Test
    public void shouldIncrementByAmount() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        redisService.increment("cnt", -1);

        // Then
        verify(valueOps).increment("cnt", -1);
    }

    @Test
    public void shouldDeleteKey() {
        // When
        redisService.deleteValue("del");

        // Then
        verify(redisTemplate).delete("del");
    }

    @Test
    public void shouldSetTTLOnKey() {
        // When
        redisService.setTTL("ttl", 7L, TimeUnit.DAYS);

        // Then
        verify(redisTemplate).expire("ttl", 7L, TimeUnit.DAYS);
    }
}
