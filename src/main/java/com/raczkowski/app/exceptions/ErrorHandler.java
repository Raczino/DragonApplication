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

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ApiException> handleUserAlreadyExists(EmailException emailException){
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST,
                emailException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ApiException> handleWrongPasswordError(WrongPasswordException wrongPasswordException){
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST,
                wrongPasswordException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
