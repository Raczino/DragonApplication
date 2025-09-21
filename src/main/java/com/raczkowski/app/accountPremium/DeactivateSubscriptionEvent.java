package com.raczkowski.app.accountPremium;

import com.raczkowski.app.events.DomainEvent;

public record DeactivateSubscriptionEvent() implements DomainEvent {
    @Override
    public String name() {
        return "DeactivateSubscriptionEvent";
    }
}
