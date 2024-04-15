package com.raczkowski.app.registration;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
public class RegistrationRequest {
    @NonNull String firstName;
    @NonNull String lastName;
    @NonNull String password;
    @NonNull String email;
}
