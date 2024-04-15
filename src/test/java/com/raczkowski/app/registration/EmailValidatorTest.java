package com.raczkowski.app.registration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    private final EmailValidator emailValidator = new EmailValidator();

    @Test
    void shouldReturnTrueForValidEmails() {
        // When-Then
        assertTrue(emailValidator.test("test@example.com"));
        assertTrue(emailValidator.test("john.doe@example.co.uk"));
        assertTrue(emailValidator.test("test123@example.com"));
    }

    @Test
    void shouldReturnFalseForInvalidEmails() {
        // When-Then
        assertFalse(emailValidator.test("not_an_email"));
        assertFalse(emailValidator.test("invalid@.com"));
        assertFalse(emailValidator.test("invalid@example"));
        assertFalse(emailValidator.test("invalid@example."));
        assertFalse(emailValidator.test("invalid@.example"));
        assertFalse(emailValidator.test("@example.com"));
        assertFalse(emailValidator.test("invalid@example..com"));
    }

    @Test
    void shouldReturnFalseForNullEmailValue() {
        // When-Then
        assertFalse(emailValidator.test(null));
    }

    @Test
    void shouldReturnFalseForEmptyEmail() {
        // When-Then
        assertFalse(emailValidator.test(""));
    }
}