package com.raczkowski.app.registration;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class EmailValidator implements Predicate<String> {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^(?=[a-zA-Z0-9@.!#$%&'*+/=?^_`{|}~-]{1,255}$)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]+)?$");

    @Override
    public boolean test(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches() && !email.contains("..");
    }
}
