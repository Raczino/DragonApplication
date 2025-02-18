package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
    public static UserDto userDto(AppUser appUser, int articlesCount, int commentsCount, int followersCount, int followingCount) {
        return new UserDto(
                appUser.getId(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getDescription(),
                appUser.getBirthDate(),
                appUser.getCity(),
                appUser.getUserRole(),
                articlesCount,
                commentsCount,
                appUser.isAccountBlocked(),
                appUser.getBlockedDate(),
                followersCount,
                followingCount,
                appUser.getAccountType()
        );
    }
}
