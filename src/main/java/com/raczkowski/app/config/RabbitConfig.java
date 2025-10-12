package com.raczkowski.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("rabbit-events")
@EnableRabbit
@Configuration
public class RabbitConfig {
    @Value("${events.exchange:dragon.events.x}")
    private String exchangeName;

    @Value("${events.queue.local:dragon.events.local.q}")
    private String queueName;

    @Bean
    TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    Queue localQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding bindAll(Queue localQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(localQueue).to(eventsExchange).with("#");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper om) {
        var c = new Jackson2JsonMessageConverter(om);
        c.setCreateMessageIds(true);
        return c;
    }
}
