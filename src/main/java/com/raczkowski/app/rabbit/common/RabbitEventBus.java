package com.raczkowski.app.rabbit.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Profile("rabbit-events")
@Component
@RequiredArgsConstructor
public class RabbitEventBus implements EventBus {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    @Value("${events.exchange:dragon.events.x}")
    String exchange;

    @Override
    public void publish(String domain, Enum<?> eventType, JsonNode payload) {
        RabbitMessage message = new RabbitMessage(
                UUID.randomUUID().toString(),
                MDC.get("corrId"),
                domain,
                eventType.name(),
                "1",
                Instant.now(),
                payload == null ? mapper.nullNode() : payload
        );

        String rk = domain + "." + eventType.name();
        rabbitTemplate.convertAndSend(exchange, rk, message, m -> {
            m.getMessageProperties().setHeader("eventEnum", eventType.getClass().getName());
            return m;
        });
    }
}
