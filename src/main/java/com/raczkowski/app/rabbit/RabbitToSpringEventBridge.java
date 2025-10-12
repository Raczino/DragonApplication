package com.raczkowski.app.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raczkowski.app.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("rabbit-events")
@Component
@RequiredArgsConstructor
public class RabbitToSpringEventBridge {
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;

    @RabbitListener(queues = "${events.queue.local:dragon.events.local.q}")
    public void handle(Message msg) throws Exception {
        var props = msg.getMessageProperties();
        var typeHeader = (String) props.getHeaders().get("eventType");
        var body = msg.getBody();

        Class<?> clazz = (typeHeader != null) ? Class.forName(typeHeader) : null;
        if (clazz == null || !DomainEvent.class.isAssignableFrom(clazz)) {
            var rk = props.getReceivedRoutingKey();
            clazz = Class.forName(props.getHeaders().getOrDefault("eventType",
                    "com.raczkowski.app.events." + rk).toString());
        }

        DomainEvent event = (DomainEvent) mapper.readValue(body, clazz);
        publisher.publishEvent(event);
    }
}
