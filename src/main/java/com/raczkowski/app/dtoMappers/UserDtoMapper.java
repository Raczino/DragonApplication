package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
    public UserDto toUserDto(AppUser user,
                             int articlesCount,
                             int commentsCount,
                             int followersCount,
                             int followingCount) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .description(user.getDescription())
                .birthDate(user.getBirthDate())
                .city(user.getCity())
                .userRole(user.getUserRole())
                .articlesCount(articlesCount)
                .commentsCount(commentsCount)
                .accountBlocked(user.isAccountBlocked())
                .blockedDate(user.getBlockedDate())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .accountType(user.getAccountType())
                .build();
    }
}
