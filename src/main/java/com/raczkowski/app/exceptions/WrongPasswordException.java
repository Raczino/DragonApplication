package com.raczkowski.app.exceptions;

public class WrongPasswordException extends RuntimeException{
    public WrongPasswordException() {
        super("Shorter than minimum length 8");
    }
}
