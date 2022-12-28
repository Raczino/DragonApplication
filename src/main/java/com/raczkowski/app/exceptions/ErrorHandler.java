package com.raczkowski.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ApplicationError> handleUserAlreadyExists(UserAlreadyExists userAlreadyExists, WebRequest webRequest){
        ApplicationError error = new ApplicationError();
        error.setName("Email");
        error.setCode(400);
        error.setDescription(userAlreadyExists.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApplicationError> handleInvalidEmail(InvalidEmailException invalidEmailException){
        ApplicationError error = new ApplicationError();
        error.setName("Email");
        error.setCode(400);
        error.setDescription(invalidEmailException.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ApplicationError> handleWrongPasswordError(WrongPasswordException wrongPasswordException){
        ApplicationError error = new ApplicationError();
        error.setName("Password");
        error.setCode(400);
        error.setDescription(wrongPasswordException.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
