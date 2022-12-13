package com.raczkowski.app.User;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final UserDetails userDetails = userRepository.findByEmail(email);
        if (userDetails == null) {
            throw new UsernameNotFoundException(email);
        }
        return User.withUsername(userDetails.getUsername())
                .password(userDetails.getPassword())
                .authorities("USER").build();
    }

    public String signUpUser(AppUser appUser) {
        if (userRepository.findByEmail(appUser.getEmail())!=null) {
            throw new IllegalStateException("Email already registered");
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        userRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        //TODO: SEND EMAIL

        return token;
    }
}
