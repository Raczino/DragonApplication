package com.raczkowski.app.rabbit.consumer;

import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.rabbit.actions.ArticleAction;
import com.raczkowski.app.rabbit.common.RabbitMessage;
import com.raczkowski.app.rabbit.retry.RetryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("rabbit-events")
@RequiredArgsConstructor
public class ArticleQueueConsumer {
    private final ArticleService articleService;
    private final RetryHelper retry;

    @Value("${events.exchange:dragon.events.x}")
    private String exchange;

    @Value("${events.retry.article.delays:10000,60000,300000}")
    private List<Long> articleDelays;

    @RabbitListener(queues = "article")
    private void handle(RabbitMessage message, Message amqp) {
        if (!retry.claimOnce(message.messageId())) {
            log.warn("Duplicate message skipped: {}", message.messageId());
            return;
        }
        try {
            switch (ArticleAction.valueOf(message.actionType())) {
                case PUBLISH -> {
                    articleService.publishArticles();
                    log.info("{} event published articles", message.actionType());
                }
                default -> log.warn("Unknown article action: {}", message.actionType());
            }
        } catch (Exception ex) {
            boolean scheduled = retry.scheduleRetry(exchange, "article", message.actionType(), amqp, articleDelays);
            if (!scheduled) {
                log.warn("Max attempts reached → sending to DLQ: messageId={} action={}", message.messageId(), message.actionType());
                throw ex;
            }
            log.warn("Scheduled retry for messageId={} action={}", message.messageId(), message.actionType(), ex);
        }
    }
}
