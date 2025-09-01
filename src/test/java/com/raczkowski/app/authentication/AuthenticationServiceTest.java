package com.raczkowski.app.authentication;

import com.raczkowski.app.config.JwtUtil;
import com.raczkowski.app.dto.AuthorDto;
import com.raczkowski.app.dto.LoginResponseDto;
import com.raczkowski.app.dtoMappers.LoginResponseMapper;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginResponseMapper loginResponseMapper;

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

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        when(userService.loadUserByUsername("test@example.com")).thenReturn(user);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        when(authenticationManager.authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken &&
                                token.getPrincipal().equals("test@example.com") &&
                                token.getCredentials().equals("password")
                )
        )).thenReturn(authentication);

        when(jwtUtil.generateToken(user)).thenReturn("jwt_token");
        AuthorDto authorDto = new AuthorDto();
        authorDto.setId(1L);
        authorDto.setEmail("test@example.com");

        LoginResponseDto expectedResponse = new LoginResponseDto("jwt_token", authorDto);
        when(loginResponseMapper.toResponseDto("jwt_token", user)).thenReturn(expectedResponse);

        // when
        LoginResponseDto response = authenticationService.authenticate(request);

        // then
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals(1L, response.getUser().getId());
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // given
        AuthenticationRequest request = new AuthenticationRequest("nonexistent@example.com", "password");
        when(userService.loadUserByUsername("nonexistent@example.com")).thenReturn(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> authenticationService.authenticate(request));

        //then
        assertEquals(ErrorMessages.EMAIL_NOT_EXISTS, exception.getMessage());
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
        assertEquals(ErrorMessages.EMAIL_AND_PASSWORD_CANNOT_BE_NULL, emailException.getMessage());
        assertEquals(ErrorMessages.EMAIL_AND_PASSWORD_CANNOT_BE_NULL, passwordException.getMessage());
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
        assertEquals(ErrorMessages.INVALID_CREDENTIALS, emailException.getMessage());
        assertEquals(ErrorMessages.INVALID_CREDENTIALS, passwordException.getMessage());
    }
}