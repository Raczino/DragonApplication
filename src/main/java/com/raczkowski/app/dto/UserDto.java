package com.raczkowski.app.dto;

import com.raczkowski.app.user.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor

@Getter
@Setter
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    private UserRole userRole;

    private int articlesCount;

    private int commentsCount;

    public UserDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            UserRole userRole,
            int articlesCount,
            int commentsCount
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userRole = userRole;
        this.articlesCount = articlesCount;
        this.commentsCount = commentsCount;
    }
}
