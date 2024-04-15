package com.raczkowski.app.authentication;

import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.dtoMappers.LoginResponseMapper;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.UserService;
import com.raczkowski.app.config.JwtUtil;
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

    public LoginResponseDto authenticate(AuthenticationRequest request) {
<<<<<<< HEAD
        final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        if (userDetails != null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            ).isAuthenticated();
            return LoginResponseMapper.response(jwtUtil.generateToken(userDetails));
=======
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseException("Email and password can't be null");
        }
        if (!request.getEmail().equals("") && !request.getPassword().equals("")) {
            final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
            if (userDetails != null) {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                ).isAuthenticated();
                return LoginResponseMapper.response(jwtUtil.generateToken(userDetails));
            }
            throw new ResponseException("User with this email doesn't exists");
        } else {
            throw new ResponseException("Invalid Credentials");
>>>>>>> 36a1067e8c940c40e49b5864d11e6b4e01129b3c
        }
    }
}
