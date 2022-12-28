package com.raczkowski.app.exceptions;

public class WrongPasswordException extends RuntimeException{
    public WrongPasswordException() {
        super("Too short password");
    }
}
