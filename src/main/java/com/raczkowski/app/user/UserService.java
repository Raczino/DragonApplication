package com.raczkowski.app.user;

import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.dtoMappers.UserDtoMapper;
import com.raczkowski.app.exceptions.Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email);
    }

    public String signUpUser(AppUser appUser) {
        if (userRepository.findByEmail(appUser.getEmail()) != null) {
            throw new Exception("User already exists");
        }

        if (appUser.getPassword().length() < 8) {
            throw new Exception("Shorter than minimum length 8");
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        userRepository.save(appUser);

        return null;
    }

    public List<AppUser> loadAllUser() {
        return userRepository.findAll();
    }

    public AppUser getLoggedUser() {
        return userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
    }

    public UserDto getUserById(Long id) {
        if (!this.getLoggedUser().getUserRole().equals(UserRole.ADMIN)) {
            throw new Exception("You don't have permissions to execute this request");
        }
        return UserDtoMapper.userDto(userRepository.getAppUserById(id));
    }

    public AppUser getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
