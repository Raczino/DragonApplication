package com.raczkowski.app.accountPremium;

import com.raczkowski.app.accountPremium.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateSubscriptionListener {
    private final SubscriptionService subscriptionService;

    @EventListener
    public void on(DeactivateSubscriptionEvent e) {
        int changed = subscriptionService.checkDeactivatedSubscription();
        if (changed > 0)
            System.out.println("DeactivateSubscriptionListener: Deactivated " + changed + "subscriptions, from " + e.name() + " event");
    }
}
