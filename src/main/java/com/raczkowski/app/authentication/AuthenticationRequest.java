package com.raczkowski.app.authentication;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationRequest {
    private String email;
    private String password;
}
