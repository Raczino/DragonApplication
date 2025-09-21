package com.raczkowski.app.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventBus {
    private final ApplicationEventPublisher delegate;

    public void publishEvent(DomainEvent event) {
        delegate.publishEvent(event);
    }
}
