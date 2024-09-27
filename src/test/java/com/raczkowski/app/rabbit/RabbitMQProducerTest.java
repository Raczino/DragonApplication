package com.raczkowski.app.rabbit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

@SpringBootTest
public class RabbitMQProducerTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private RabbitMqProducer rabbitMqProducer;

    @Test
    public void testSendMessage() {
        MockitoAnnotations.openMocks(this);

        String exchange = "redditExchange";
        String routingKey = "reddit.#";
        String message = "testQueue";

        // Wywołanie metody sendMessage
        rabbitMqProducer.sendMessage(exchange, routingKey, message);

        // Weryfikacja, czy amqpTemplate.convertAndSend() zostało wywołane z poprawnymi argumentami
        verify(amqpTemplate).convertAndSend(exchange, routingKey, message);
    }
}
