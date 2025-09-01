package com.raczkowski.app.user;

import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldLoadUserByUsernameViaRepository() {
        // Given
        AppUser user = new AppUser();
        when(userRepository.findByEmail("a@b.com")).thenReturn(user);

        // When
        UserDetails out = userService.loadUserByUsername("a@b.com");

        // Then
        assertSame(user, out);
        verify(userRepository).findByEmail("a@b.com");
    }

    @Test
    public void shouldThrowIfEmailExists() {
        // Given
        when(userRepository.findByEmail("dup@x.com")).thenReturn(new AppUser());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> userService.checkIfEmailExists("dup@x.com"));
        assertEquals(ErrorMessages.USER_ALREADY_EXITS, ex.getMessage());
    }

    @Test
    public void shouldNotThrowIfEmailNotExists() {
        // Given
        when(userRepository.findByEmail("free@x.com")).thenReturn(null);

        // When / Then
        assertDoesNotThrow(() -> userService.checkIfEmailExists("free@x.com"));
        verify(userRepository).findByEmail("free@x.com");
    }

    @Test
    public void shouldEncodePasswordAndSaveUserOnSignUp() {
        // Given
        AppUser user = new AppUser();
        user.setPassword("raw");
        when(bCryptPasswordEncoder.encode("raw")).thenReturn("ENC");

        // When
        String result = userService.signUpUser(user);

        // Then
        assertNull(result);
        assertEquals("ENC", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void shouldReturnLoggedUserFromSecurityContextEmail() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        AppUser me = new AppUser();
        when(userRepository.findByEmail("me@x.com")).thenReturn(me);

        // When
        AppUser out = userService.getLoggedUser();

        // Then
        assertSame(me, out);
        verify(userRepository).findByEmail("me@x.com");
    }

    @Test
    public void shouldReturnFollowersCount() {
        // Given
        AppUser u = new AppUser();
        u.setId(10L);
        when(userRepository.getAppUserById(10L)).thenReturn(u);
        when(userRepository.findFollowersByUserId(10L)).thenReturn(List.of(new AppUser(), new AppUser()));

        // When
        int count = userService.userFollowersCount(u);

        // Then
        assertEquals(2, count);
    }

    @Test
    public void shouldReturnFollowingCount() {
        // Given
        AppUser u = new AppUser();
        u.setId(11L);
        when(userRepository.getAppUserById(11L)).thenReturn(u);
        when(userRepository.findFollowingByUserId(11L)).thenReturn(List.of(new AppUser()));

        // When
        int count = userService.userFollowingCount(u);

        // Then
        assertEquals(1, count);
    }

    @Test
    public void shouldGetUserById() {
        // Given
        AppUser u = new AppUser();
        when(userRepository.getAppUserById(5L)).thenReturn(u);

        // When
        AppUser out = userService.getUserById(5L);

        // Then
        assertSame(u, out);
        verify(userRepository).getAppUserById(5L);
    }

    @Test
    public void shouldGetUserByEmail() {
        // Given
        AppUser u = new AppUser();
        when(userRepository.findByEmail("x@y.com")).thenReturn(u);

        // When
        AppUser out = userService.getUserByEmail("x@y.com");

        // Then
        assertSame(u, out);
        verify(userRepository).findByEmail("x@y.com");
    }

    @Test
    public void shouldGetFollowersList() {
        // Given
        AppUser a = new AppUser();
        AppUser b = new AppUser();
        when(userRepository.findFollowersByUserId(9L)).thenReturn(List.of(a, b));

        // When
        List<AppUser> out = userService.getFollowersCount(9L);

        // Then
        assertEquals(2, out.size());
        verify(userRepository).findFollowersByUserId(9L);
    }

    @Test
    public void shouldGetFollowingUsersList() {
        // Given
        AppUser a = new AppUser();
        when(userRepository.findFollowingByUserId(8L)).thenReturn(List.of(a));

        // When
        List<AppUser> out = userService.getFollowingUsersByUserCount(8L);

        // Then
        assertEquals(1, out.size());
        verify(userRepository).findFollowingByUserId(8L);
    }

    @Test
    public void shouldThrowWhenFollowTargetNotFound() {
        // Given
        AppUser current = new AppUser();

        when(userRepository.findByEmail(anyString())).thenReturn(current);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> userService.followUser(100L));
        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenTryingToFollowYourself() {
        // Given
        AppUser current = new AppUser();
        // getLoggedUser -> SecurityContext email
        when(userRepository.findByEmail(anyString())).thenReturn(current);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(1L)).thenReturn(Optional.of(current));

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> userService.followUser(1L));
        assertEquals(ErrorMessages.CANNOT_FOLLOW_YOURSELF, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldFollowUserAndSaveCurrentUser() {
        // Given
        AppUser current = spy(new AppUser());
        AppUser target = new AppUser();
        when(userRepository.findByEmail(anyString())).thenReturn(current);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        // When
        userService.followUser(2L);

        // Then
        verify(userRepository).save(current);
    }

    @Test
    public void shouldThrowWhenUnfollowTargetNotFound() {
        // Given
        AppUser current = new AppUser();
        when(userRepository.findByEmail(anyString())).thenReturn(current);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(200L)).thenReturn(Optional.empty());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> userService.unfollowUser(200L));
        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldUnfollowUserAndSaveCurrentUser() {
        // Given
        AppUser current = spy(new AppUser());
        AppUser target = new AppUser();
        when(userRepository.findByEmail(anyString())).thenReturn(current);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(3L)).thenReturn(Optional.of(target));

        // When
        userService.unfollowUser(3L);

        // Then
        verify(current).unfollowUser(target);
        verify(userRepository).save(current);
    }

    @Test
    public void shouldUpdateAppUserRole() {
        // When
        userService.updateAppUserByUserRole(7L, UserRole.MODERATOR);

        // Then
        verify(userRepository).updateAppUserByUserRole(7L, UserRole.MODERATOR);
    }

    @Test
    public void shouldBlockUser() {
        // Given
        ZonedDateTime now = ZonedDateTime.now();

        // When
        userService.blockUser(9L, now);

        // Then
        verify(userRepository).blockUser(9L, now);
    }

    @Test
    public void shouldUnblockUser() {
        // When
        userService.unblockUser(9L);

        // Then
        verify(userRepository).unBlockUser(9L);
    }
}
