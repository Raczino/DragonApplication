package com.raczkowski.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class RegistrationResponseDto {
    private String token;

    private UserDto user;
}
