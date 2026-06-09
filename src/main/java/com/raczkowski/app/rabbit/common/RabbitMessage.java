package com.raczkowski.app.rabbit.common;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record RabbitMessage(
        String messageId,
        String correlationId,
        String domain,
        String actionType,
        String version,
        Instant occurredAt,
        JsonNode payload
) {
}
