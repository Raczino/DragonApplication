package com.raczkowski.app.exceptions;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ApiException {
    private final int status;
    private final String description;
    private final ZonedDateTime timestamp;
    public ApiException(int status, String description, ZonedDateTime timestamp) {
        this.status = status;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}
