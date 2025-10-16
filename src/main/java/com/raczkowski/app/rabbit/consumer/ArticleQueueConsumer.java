package com.raczkowski.app.rabbit.consumer;

import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.rabbit.actions.ArticleAction;
import com.raczkowski.app.rabbit.common.RabbitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("rabbit-events")
@RequiredArgsConstructor
public class ArticleQueueConsumer {
    private final ArticleService articleService;

    @RabbitListener(queues = "article")
    private void handle(RabbitMessage message) {
        try {
            switch (ArticleAction.valueOf(message.actionType())) {
                case PUBLISH -> {
                    articleService.publishArticles();
                    log.info("{} event published articles", message.actionType());
                }
                default -> log.warn("Unknown article action: {}", message.actionType());
            }
        } catch (Exception ex) {
            log.error("Error handling article messageId={} action={} → DLQ", message.messageId(), message.actionType(), ex);
            throw ex;
        }
    }
}
