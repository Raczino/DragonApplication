package com.raczkowski.app.registration;

@FunctionalInterface
public interface RegistrationValidator {
    boolean test(String string);
}
