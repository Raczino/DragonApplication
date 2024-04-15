package com.raczkowski.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
@RestController
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<ApiException> handleException(ResponseException responseException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                responseException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
