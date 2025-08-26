package com.raczkowski.app.dto;

import com.raczkowski.app.dtoMappers.UserDtoMapper;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserStatisticsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserDtoAssembler {
    private final UserStatisticsService stats;
    private final UserDtoMapper mapper;

    public UserDto assemble(AppUser user) {
        return mapper.toUserDto(
                user,
                stats.getArticlesCount(user),
                stats.getCommentsCount(user),
                stats.getFollowersCount(user),
                stats.getFollowingCount(user)
        );
    }
}
