package com.raczkowski.app.registration;

import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;
    private final DefaultPasswordValidator passwordValidator;

    public String register(RegistrationRequest request) {
        userService.checkIfEmailExists(request.getEmail());
        if (!emailValidator.test(request.getEmail())) {
            throw new ResponseException(ErrorMessages.INVALID_EMAIL);
        }
        if (!passwordValidator.test(request.getPassword())) {
            throw new ResponseException(ErrorMessages.INVALID_PASSWORD);
        }

        return userService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getDescription(),
                        request.getBirthDate(),
                        request.getCity(),
                        ZonedDateTime.now(ZoneOffset.UTC)
                )
        );
    }
}
