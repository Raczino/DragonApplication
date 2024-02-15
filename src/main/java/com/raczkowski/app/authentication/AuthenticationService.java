package com.raczkowski.app.authentication;

import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.dtoMappers.LoginResponseMapper;
import com.raczkowski.app.exceptions.Exception;
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
        final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        if (userDetails != null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            ).isAuthenticated();
            return LoginResponseMapper.response(jwtUtil.generateToken(userDetails), userService.getUserByEmail(request.getEmail()));
        }
        throw new Exception("User with this email doesn't exists");
    }
}
