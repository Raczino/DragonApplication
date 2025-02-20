package com.raczkowski.app.authentication;

import com.raczkowski.app.config.JwtUtil;
import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import com.sun.security.auth.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    public AuthenticationServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnLoginResponseDtoWhenUserExists() {
        // given
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setUserRole(UserRole.USER);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(userService.loadUserByUsername("test@example.com")).thenReturn(user);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);  // Mockowanie getUserByEmail
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(user)).thenReturn("jwt_token");

        // when
        LoginResponseDto response = authenticationService.authenticate(request);

        // then
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals(1L, response.getUser().getId());  // Sprawdzamy, czy ID użytkownika się zgadza
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // given
        AuthenticationRequest request = new AuthenticationRequest("nonexistent@example.com", "password");
        when(userService.loadUserByUsername("nonexistent@example.com")).thenReturn(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> authenticationService.authenticate(request));

        //then
        assertEquals("User with this email doesn't exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenEmailOrPasswordEqualsNull() {
        // given
        AuthenticationRequest requestWithNullEmailValue = new AuthenticationRequest(null, "password");
        AuthenticationRequest requestWithNullPasswordValue = new AuthenticationRequest("email@email.com", null);

        //when
        Exception emailException = assertThrows(Exception.class, () -> authenticationService.authenticate(requestWithNullEmailValue));
        Exception passwordException = assertThrows(Exception.class, () -> authenticationService.authenticate(requestWithNullPasswordValue));
        //then
        assertEquals("Email and password can't be null", emailException.getMessage());
        assertEquals("Email and password can't be null", passwordException.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenEmailOrPasswordEqualsEmpty() {
        // given
        AuthenticationRequest requestWithNullEmailValue = new AuthenticationRequest("", "password");
        AuthenticationRequest requestWithNullPasswordValue = new AuthenticationRequest("email@email.com", "");

        //when
        Exception emailException = assertThrows(Exception.class, () -> authenticationService.authenticate(requestWithNullEmailValue));
        Exception passwordException = assertThrows(Exception.class, () -> authenticationService.authenticate(requestWithNullPasswordValue));
        //then
        assertEquals("Invalid Credentials", emailException.getMessage());
        assertEquals("Invalid Credentials", passwordException.getMessage());
    }
}