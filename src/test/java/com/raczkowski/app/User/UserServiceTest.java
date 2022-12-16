package com.raczkowski.app.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserService userService;
    @Mock private AppUser appUser;

    @BeforeEach
    void setUp(){
        userService = new UserService(userRepository,bCryptPasswordEncoder);
    }

    @Test
    void shouldSignUpUser(){
        //when
        userService.signUpUser(appUser);
        //then
        verify(userRepository).findByEmail(appUser.getEmail());
    }

    @Test
    void shouldLoadAllUser() {
        //when
        userService.loadAllUser();
        //then
        verify(userRepository).findAll();
    }
}