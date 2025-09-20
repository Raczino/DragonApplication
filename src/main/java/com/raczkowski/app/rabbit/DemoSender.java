package com.raczkowski.app.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import static com.raczkowski.app.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
public class DemoSender {

    private final AmqpTemplate amqpTemplate;

    public void send(String msg) {
        amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, msg);
    }
}
