package com.raczkowski.app.authentication;

import com.raczkowski.app.config.JwtUtil;
import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.dtoMappers.LoginResponseMapper;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.UserService;
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
    private final LoginResponseMapper loginResponseMapper;

    public LoginResponseDto authenticate(AuthenticationRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseException(ErrorMessages.EMAIL_AND_PASSWORD_CANNOT_BE_NULL);
        }
        if (!request.getEmail().isEmpty() && !request.getPassword().isEmpty()) {
            final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
            if (userDetails != null) {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                ).isAuthenticated();
                return loginResponseMapper.toResponseDto(jwtUtil.generateToken(userDetails), userService.getUserByEmail(userDetails.getUsername()));
            }
            throw new ResponseException(ErrorMessages.EMAIL_NOT_EXISTS);
        } else {
            throw new ResponseException(ErrorMessages.INVALID_CREDENTIALS);
        }
    }
}
