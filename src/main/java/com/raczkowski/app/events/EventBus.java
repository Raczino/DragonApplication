package com.raczkowski.app.events;

public interface EventBus {
    void publish(DomainEvent event);
}
