package com.raczkowski.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
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
    public ResponseEntity<ApiException> handleUserAlreadyExists(EmailException emailException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                emailException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ApiException> handleWrongPasswordError(WrongPasswordException wrongPasswordException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                wrongPasswordException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArticleException.class)
    public ResponseEntity<ApiException> handleArticleException(ArticleException articleException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                articleException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CommentException.class)
    public ResponseEntity<ApiException> handleCommentException(CommentException commentException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                commentException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z")) //TODO: zrobić globalny format dat do mniejszej ilości liczb po przecinku
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiException> userException(UserException userException) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                userException.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
