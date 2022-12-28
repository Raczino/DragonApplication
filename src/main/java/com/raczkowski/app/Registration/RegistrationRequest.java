package com.raczkowski.app.Registration;

import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    @NonNull
    private final String firstName;
    @NonNull
    private final String lastName;
    @NonNull
    private final String password;
    @NonNull
    private final String email;
}
