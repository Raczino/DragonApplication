package com.raczkowski.app.rabbit.common;

import com.fasterxml.jackson.databind.JsonNode;

public interface EventBus {
    void publish(String domain, Enum<?> eventType, JsonNode payload);
}
