package com.raczkowski.app.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class ApiException {
    private final int status;
    private final String description;
    private final ZonedDateTime timestamp;
}
