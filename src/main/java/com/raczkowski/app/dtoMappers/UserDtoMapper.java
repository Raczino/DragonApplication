package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
    public static UserDto userDto(AppUser appUser) {
        return new UserDto(
                appUser.getId(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getDescription(),
                appUser.getBirthDate(),
                appUser.getCity(),
                appUser.getUserRole(),
                appUser.getArticlesCount(),
                appUser.getCommentsCount(),
                appUser.isAccountBlocked(),
                appUser.getBlockedDate()
        );
    }
}
