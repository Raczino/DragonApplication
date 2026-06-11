package com.raczkowski.app.rabbit.retry;

import com.raczkowski.app.config.EventsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Profile("rabbit-events")
@Component
@RequiredArgsConstructor
public class RetryHelper {
    private static final String HDR_RETRY = "x-retry-count";
    private static final String HDR_ACTION = "x-action";

    /**
     * Extra time the retry queue is kept alive beyond the message TTL, so the broker
     * never deletes the queue (via x-expires) while a message is still waiting in it.
     */
    private static final long EXPIRE_BUFFER_MS = 60_000L;

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redis;
    private final AmqpAdmin amqpAdmin;
    private final EventsProperties events;

    /**
     * Reads how many times this message has already been retried.
     * 0 on the first delivery, then 1, 2, ... for each scheduled retry.
     */
    public int currentAttempt(Message message) {
        if (message == null) {
            return 0;
        }
        return parseAttempt(message.getMessageProperties().getHeaders().get(HDR_RETRY));
    }

    /**
     * Idempotency guard. The key includes the attempt so a scheduled retry of the
     * same message is NOT mistaken for a duplicate of its earlier delivery.
     */
    public boolean claimOnce(String messageId, int attempt) {
        if (messageId == null) {
            return true;
        }
        String key = "events:msg:" + messageId + ":" + attempt;
        Boolean set = redis.opsForValue().setIfAbsent(key, "1", events.getDedup().getTtlSec(), TimeUnit.SECONDS);
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
        int attempt = parseAttempt(props.getHeaders().get(HDR_RETRY)) + 1;

        if (attempt > events.getRetry().getMaxAttempts()) {
            return false;
        }

        long delay = delays.get(Math.min(attempt - 1, delays.size() - 1));
        String retryRoutingKey = domain + ".retry." + delay + "ms";

        declareEphemeralRetryQueue(exchange, domain, delay, retryRoutingKey);

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

    /**
     * Declares the per-delay retry queue on demand. The queue:
     * - holds the message for {@code delay} ms (x-message-ttl),
     * - dead-letters it back to the main domain queue afterwards,
     * - auto-deletes itself once empty and unused (x-expires = delay + buffer).
     * Re-declaring an already existing queue with identical args is a no-op and
     * resets its expiry timer, so the queue lives only while retries keep flowing.
     */
    private void declareEphemeralRetryQueue(String exchange, String domain, long delay, String retryRoutingKey) {
        String retryQueueName = domain + ".retry." + delay + "ms";

        Queue retryQueue = QueueBuilder.durable(retryQueueName)
                .withArgument("x-message-ttl", delay)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", domain)
                .withArgument("x-expires", delay + EXPIRE_BUFFER_MS)
                .build();

        Binding binding = BindingBuilder.bind(retryQueue).to(new TopicExchange(exchange)).with(retryRoutingKey);

        amqpAdmin.declareQueue(retryQueue);
        amqpAdmin.declareBinding(binding);
    }

    private int parseAttempt(Object hdr) {
        if (hdr instanceof Number) {
            return ((Number) hdr).intValue();
        }
        if (hdr instanceof String) {
            try {
                return Integer.parseInt((String) hdr);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
