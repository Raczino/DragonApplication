package com.raczkowski.app.messaging;

import com.raczkowski.app.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoListener {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleMessage(String payload) {
        log.info("Received from RabbitMQ: {}", payload);
    }
}