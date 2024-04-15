package com.raczkowski.app.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AppUser implements UserDetails {

    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    @Getter
    private String firstName;

    @Getter
    private String lastName;

    private String email;

    private String password;

    private String description;

    private String birthDate;

    private String city;

    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    private Boolean locked = false;

    private Boolean enabled = false;

    @Getter
    private int articlesCount = 0;

    @Getter
    private int commentsCount = 0;

    private ZonedDateTime registrationDate;

    public AppUser(String firstName,
                   String lastName,
                   String email,
                   String password,
                   String description,
                   String birthDate,
                   String city,
                   ZonedDateTime registrationDate
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.description = description;
        this.birthDate = birthDate;
        this.city = city;
        this.registrationDate = registrationDate;
    }

    public AppUser(String firstName,
                   String lastName,
                   String email
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
