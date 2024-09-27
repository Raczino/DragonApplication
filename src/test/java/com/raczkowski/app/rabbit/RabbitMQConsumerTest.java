package com.raczkowski.app.rabbit;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

@SpringBootTest
public class RabbitMQConsumerTest {

    private final RabbitMqConsumer rabbitMqConsumer = new RabbitMqConsumer();

    @Test
    public void testReceiveMessage() {
        // Given
        String testMessage = "Test message";

        // When
        String message = rabbitMqConsumer.receiveMessage(testMessage); // Wywołanie metody odbioru wiadomości

        // Then
        // Możesz tutaj sprawdzić, czy metoda została wywołana lub jakieś inne efekty
        System.out.println(message);
    }
}