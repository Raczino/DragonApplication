package com.raczkowski.app.authentication;

import com.raczkowski.app.User.UserService;
import com.raczkowski.app.config.JwtUtil;
import com.raczkowski.app.exceptions.EmailException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public String authenticate(AuthenticationRequest request) {
        final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        if (userDetails != null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            return jwtUtil.generateToken(userDetails);
        }
        throw new EmailException("User with this email doesn't exists");
    }
}
