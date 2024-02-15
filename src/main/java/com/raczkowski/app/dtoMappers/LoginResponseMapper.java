package com.raczkowski.app.dto;

import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class RegistrationResponse {
    public static RegistrationResponseDto response(String token, AppUser appUser) {
        return new RegistrationResponseDto(
                token,
                new UserDto(appUser.getId(),
                        appUser.getFirstName(),
                        appUser.getLastName(),
                        appUser.getEmail(),
                        appUser.getUserRole(),
                        appUser.getArticlesCount(),
                        appUser.getCommentsCount())
        );
    }
}
