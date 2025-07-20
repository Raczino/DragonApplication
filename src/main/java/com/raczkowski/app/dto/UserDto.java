package com.raczkowski.app.dto;

import com.raczkowski.app.enums.AccountType;
import com.raczkowski.app.enums.UserRole;
import lombok.*;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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
    private boolean accountBlocked;
    private ZonedDateTime blockedDate;
    private int followersCount;
    private int followingCount;
    private AccountType accountType;
}
