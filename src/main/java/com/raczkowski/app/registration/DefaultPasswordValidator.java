package com.raczkowski.app.registration;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DefaultPasswordValidator implements RegistrationValidator {

    private static final int MIN_LEN = 8;
    private static final int MAX_LEN = 64;

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    @Override
    public boolean test(String p) {
        if (p == null) return false;
        if (p.length() < MIN_LEN || p.length() > MAX_LEN) return false;
        if (WHITESPACE.matcher(p).find()) return false;
        if (!UPPER.matcher(p).find()) return false;
        if (!LOWER.matcher(p).find()) return false;
        if (!DIGIT.matcher(p).find()) return false;
        if (!SPECIAL.matcher(p).find()) return false;
        return true;
    }
}
