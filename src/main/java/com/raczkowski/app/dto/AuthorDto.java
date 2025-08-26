package com.raczkowski.app.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isBlocked;
}
