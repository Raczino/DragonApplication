package com.raczkowski.app.rabbit.consumer;

import com.raczkowski.app.accountPremium.service.SubscriptionService;
import com.raczkowski.app.rabbit.actions.SubscriptionAction;
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
public class SubscriptionQueueConsumer {
    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = "subscription")
    private void handle(RabbitMessage message) {
        try {
            switch (SubscriptionAction.valueOf(message.actionType())) {
                case DEACTIVATE -> {
                    subscriptionService.checkDeactivatedSubscription();
                    log.info("{} event deactivate subscriptions", message.actionType());
                }
                default -> log.warn("Unknown subscriptions action: {}", message.actionType());
            }
        } catch (Exception ex) {
            log.error("Error handling subscription messageId={} action={} → DLQ", message.messageId(), message.actionType(), ex);
            throw ex;
        }
    }
}
