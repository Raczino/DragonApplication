package com.raczkowski.app.rabbit.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryHelper {
    private static final String HDR_RETRY = "x-retry-count";
    private static final String HDR_ACTION = "x-action";

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redis;

    @Value("${events.retry.maxAttempts:5}")
    private int maxAttempts;

    @Value("${events.dedup.ttl-sec:3600}")
    private long dedupTtlSec;

    public boolean claimOnce(String messageId) {
        if (messageId == null) return true;
        String key = "events:msg:" + messageId;
        Boolean set = redis.opsForValue().setIfAbsent(key, "1", dedupTtlSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(set);
    }

    public boolean scheduleRetry(String exchange,
                                 String domain,
                                 String actionEnumName,
                                 Message original,
                                 List<Long> delays) {

        Objects.requireNonNull(exchange);
        Objects.requireNonNull(domain);
        Objects.requireNonNull(original);
        if (delays == null || delays.isEmpty()) {
            log.warn("No delays configured for domain {}", domain);
            return false;
        }

        MessageProperties props = original.getMessageProperties();
        int attempt = 0;
        Object hdr = props.getHeaders().get(HDR_RETRY);
        if (hdr instanceof Number) {
            attempt = ((Number) hdr).intValue();
        } else if (hdr instanceof String) {
            try {
                attempt = Integer.parseInt((String) hdr);
            } catch (NumberFormatException ignored) {
            }
        }
        attempt++;

        if (attempt > maxAttempts) {
            return false;
        }

        long delay = delays.get(Math.min(attempt - 1, delays.size() - 1));
        String retryRoutingKey = domain + ".retry." + delay + "ms";

        Message toSend = MessageBuilder
                .withBody(original.getBody())
                .andProperties(props)
                .setHeader(HDR_RETRY, attempt)
                .setHeader(HDR_ACTION, actionEnumName)
                .build();

        log.warn("Scheduling retry domain={} action={} attempt={} delay={}ms rk={}",
                domain, actionEnumName, attempt, delay, retryRoutingKey);

        rabbitTemplate.send(exchange, retryRoutingKey, toSend);
        return true;
    }

    public boolean claimCustom(String key, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
}
