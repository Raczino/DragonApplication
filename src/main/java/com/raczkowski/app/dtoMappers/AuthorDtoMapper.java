package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.AuthorDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AuthorDtoMapper {

    public AuthorDto toAuthorDto(AppUser user) {
        if (user == null) return null;

        return AuthorDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isBlocked(user.isAccountBlocked())
                .build();
    }
}
