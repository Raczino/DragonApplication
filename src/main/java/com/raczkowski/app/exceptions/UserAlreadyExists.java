package com.raczkowski.app.exceptions;

public class UserAlreadyExists extends RuntimeException{
    public UserAlreadyExists() {
        super("Email already exists");
    }
}
