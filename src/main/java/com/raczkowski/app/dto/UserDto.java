package com.raczkowski.app.dto;

import com.raczkowski.app.User.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    private int articlesCount;

    private int commentsCount;

    public UserDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            UserRole role,
            int articlesCount,
            int commentsCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.articlesCount = articlesCount;
        this.commentsCount = commentsCount;
    }
}
