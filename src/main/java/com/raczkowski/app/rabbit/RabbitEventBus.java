package com.raczkowski.app.rabbit;

import com.raczkowski.app.events.DomainEvent;
import com.raczkowski.app.events.EventBus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("rabbit-events")
@Component
@RequiredArgsConstructor
public class RabbitEventBus implements EventBus {
    private final RabbitTemplate rabbitTemplate;

    @Value("${events.exchange:dragon.events.x}")
    String exchange;

    @Override
    public void publish(DomainEvent event) {
        rabbitTemplate.convertAndSend(
                exchange,
                event.getClass().getSimpleName(),
                event,
                m -> {
                    m.getMessageProperties().setHeader("eventType", event.getClass().getName());
                    return m;
                }
        );
    }
}
