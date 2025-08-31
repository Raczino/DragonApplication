package com.raczkowski.app.registration;

import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private DefaultPasswordValidator passwordValidator;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    public void shouldThrowWhenEmailInvalid() {
        // Given
        RegistrationRequest req = new RegistrationRequest();
        req.setFirstName("Ana");
        req.setLastName("Nowak");
        req.setEmail("bad-email");
        req.setPassword("Abcdef1!");
        req.setDescription("desc");
        req.setBirthDate("2000-01-02");
        req.setCity("Warsaw");

        when(emailValidator.test("bad-email")).thenReturn(false);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> registrationService.register(req));
        assertEquals(ErrorMessages.INVALID_EMAIL, ex.getMessage());

        verify(passwordValidator, never()).test(anyString());
        verify(userService, never()).signUpUser(any());
    }

    @Test
    public void shouldThrowWhenPasswordInvalid() {
        // Given
        RegistrationRequest req = new RegistrationRequest();
        req.setFirstName("Ana");
        req.setLastName("Nowak");
        req.setEmail("ana@example.com");
        req.setPassword("weak");
        req.setBirthDate("2000-01-02");
        req.setCity("Warsaw");

        when(emailValidator.test("ana@example.com")).thenReturn(true);
        when(passwordValidator.test("weak")).thenReturn(false);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> registrationService.register(req));
        assertEquals(ErrorMessages.INVALID_PASSWORD, ex.getMessage());

        verify(userService, never()).signUpUser(any());
    }

    @Test
    public void shouldRegisterWhenEmailAndPasswordValid() {
        // Given
        RegistrationRequest req = new RegistrationRequest();
        req.setFirstName("Jan");
        req.setLastName("Kowalski");
        req.setEmail("jan.kowalski@example.com");
        req.setPassword("Abcdef1!");
        req.setDescription("hello");
        req.setBirthDate("1995-05-20");
        req.setCity("Kraków");

        when(emailValidator.test(req.getEmail())).thenReturn(true);
        when(passwordValidator.test(req.getPassword())).thenReturn(true);
        when(userService.signUpUser(any(AppUser.class))).thenReturn("OK");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        // When
        String result = registrationService.register(req);

        // Then
        assertEquals("OK", result);
        verify(userService).signUpUser(userCaptor.capture());
        AppUser saved = userCaptor.getValue();
        assertEquals("Jan", saved.getFirstName());
        assertEquals("Kowalski", saved.getLastName());
        assertEquals("jan.kowalski@example.com", saved.getEmail());
        assertEquals("Abcdef1!", saved.getPassword());
        assertEquals("hello", saved.getDescription());
        assertEquals("1995-05-20", saved.getBirthDate());
        assertEquals("Kraków", saved.getCity());

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        assertNotNull(saved.getRegistrationDate());
        assertEquals(ZoneOffset.UTC, saved.getRegistrationDate().getOffset());
        assertTrue(!saved.getRegistrationDate().isBefore(nowUtc.minusSeconds(5)) &&
                !saved.getRegistrationDate().isAfter(nowUtc.plusSeconds(5)));
    }
}
