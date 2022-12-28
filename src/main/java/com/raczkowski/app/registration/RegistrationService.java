package com.raczkowski.app.registration;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserService;
import com.raczkowski.app.exceptions.EmailException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail) {
            throw new EmailException("Invalid Email");
        }
        return userService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        ZonedDateTime.now(ZoneOffset.UTC)
                )
        );
    }
}
