package com.raczkowski.app.dto;

import com.raczkowski.app.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String description;

    private String birthDate;

    private String city;

    private UserRole userRole;

    private int articlesCount;

    private int commentsCount;

    private boolean isBlocked;

    private ZonedDateTime blockedDate;
}
