package com.raczkowski.app.registration;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationRequest {
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String password;
    @NonNull
    private String email;

    private String description;

    private String birthDate;

    private String city;
}
