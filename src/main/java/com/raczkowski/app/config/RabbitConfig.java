package com.raczkowski.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("rabbit-events")
@EnableRabbit
@Configuration
public class RabbitConfig {
    @Value("${events.exchange:dragon.events.x}")
    private String exchangeName;

    @Bean
    TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    Queue articleQueue() {
        return QueueBuilder.durable("article")
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "article.dlq")
                .build();
    }

    @Bean
    Queue subscriptionQueue() {
        return QueueBuilder.durable("subscription")
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "subscription.dlq")
                .build();
    }

    @Bean
    Binding articleBinding(Queue articleQueue, TopicExchange topic) {
        return BindingBuilder.bind(articleQueue).to(topic).with("article.*");
    }

    @Bean
    Binding subscriptionBinding(Queue subscriptionQueue, TopicExchange topic) {
        return BindingBuilder.bind(subscriptionQueue).to(topic).with("subscription.*");
    }

    @Bean
    Queue articleDlq() {
        return QueueBuilder.durable("article.dlq").build();
    }

    @Bean
    Binding articleDlqBinding(Queue articleDlq, TopicExchange x) {
        return BindingBuilder.bind(articleDlq).to(x).with("article.dlq");
    }

    @Bean
    Queue subscriptionDlq() {
        return QueueBuilder.durable("subscription.dlq").build();
    }

    @Bean
    Binding subscriptionDlqBinding(Queue subscriptionDlq, TopicExchange x) {
        return BindingBuilder.bind(subscriptionDlq).to(x).with("subscription.dlq");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }
}
