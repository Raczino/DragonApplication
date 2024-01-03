package com.raczkowski.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TokenDto {
    private String token;

    public TokenDto(String token) {
        this.token = token;
    }
}
