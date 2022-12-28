package com.raczkowski.app.Registration;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserService;
import com.raczkowski.app.exceptions.InvalidEmailException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail) {
            throw new InvalidEmailException();
        }
        return userService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }
}
