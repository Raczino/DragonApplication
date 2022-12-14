package com.raczkowski.app.dto;

import com.raczkowski.app.User.UserRole;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AppUserDao {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole = UserRole.USER;
}
