package com.raczkowski.app.registration;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
public class RegistrationRequest {
<<<<<<< HEAD
    @NonNull
    private final String firstName;
    @NonNull
    private final String lastName;
    @NonNull
    private final String password;
    @NonNull
    private final String email;

    private final String description;

    private final String birthDate;

    private final String city;
=======
    @NonNull String firstName;
    @NonNull String lastName;
    @NonNull String password;
    @NonNull String email;
>>>>>>> 36a1067e8c940c40e49b5864d11e6b4e01129b3c
}
