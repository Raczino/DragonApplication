package com.raczkowski.app.exceptions;

public class InvalidEmailException extends RuntimeException{
    public InvalidEmailException() {
        super("Invalid Email");
    }
}
