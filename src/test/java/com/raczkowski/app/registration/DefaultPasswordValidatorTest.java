package com.raczkowski.app.registration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultPasswordValidatorTest {

    private final RegistrationValidator validator = new DefaultPasswordValidator();

    @Test
    public void shouldAcceptStrongPassword() {
        assertTrue(validator.test("Abcdef1!"));
        assertTrue(validator.test("Very$trongP4ssword"));
    }

    @Test
    public void shouldRejectNullOrTooShortOrTooLong() {
        assertFalse(validator.test(null));
        assertFalse(validator.test("A1!a"));
        assertFalse(validator.test("A" + "a".repeat(200) + "1!"));
    }

    @Test
    public void shouldRejectWhenMissingRequiredClasses() {
        assertFalse(validator.test("abcdef1!"));
        assertFalse(validator.test("ABCDEF1!"));
        assertFalse(validator.test("Abcdefg!"));
        assertFalse(validator.test("Abcdefg1"));
    }

    @Test
    public void shouldRejectWhenContainsWhitespace() {
        assertFalse(validator.test("Abcdef1! "));
        assertFalse(validator.test("Abcd ef1!"));
        assertFalse(validator.test("Abcdef1!\n"));
    }
}