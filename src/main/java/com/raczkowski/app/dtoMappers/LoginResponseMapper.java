package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginResponseMapper {

    private final AuthorDtoMapper authorDtoMapper;

    public LoginResponseDto toResponseDto(String token, AppUser user) {
        return new LoginResponseDto(
                token,
                authorDtoMapper.toAuthorDto(user)
        );
    }
}
