package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.AuthorDto;
import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {
    public static LoginResponseDto response(String token, AppUser user) {
        return new LoginResponseDto(
                token,
                new AuthorDto(user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.isAccountBlocked())
        );
    }
}
