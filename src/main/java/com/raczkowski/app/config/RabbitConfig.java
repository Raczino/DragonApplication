package com.raczkowski.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("rabbit-events")
@EnableRabbit
@Configuration
@EnableConfigurationProperties(EventsProperties.class)
public class RabbitConfig {

    @Bean
    TopicExchange eventsExchange(EventsProperties events) {
        return ExchangeBuilder.topicExchange(events.getExchange()).durable(true).build();
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }
}