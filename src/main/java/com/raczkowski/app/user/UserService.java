package com.raczkowski.app.user;

import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

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
            throw new ResponseException("User already exists");
        }

        if (appUser.getPassword().length() < 8) {
            throw new ResponseException("Shorter than minimum length 8");
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

    public int userFollowersCount(AppUser user) {
        return userRepository.findFollowersByUserId(getUserById(user.getId()).getId()).size();
    }

    public int userFollowingCount(AppUser user) {
        return userRepository.findFollowingByUserId(getUserById(user.getId()).getId()).size();
    }

    public AppUser getUserById(Long id) {
        return userRepository.getAppUserById(id);
    }

    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<AppUser> getFollowersCount(Long userId) {
        return userRepository.findFollowersByUserId(userId);
    }

    public List<AppUser> getFollowingUsersByUserCount(Long userId) {
        return userRepository.findFollowingByUserId(userId);
    }

    public void followUser(Long userIdToFollow) {
        AppUser currentUser = getLoggedUser();
        AppUser userToFollow = userRepository.findById(userIdToFollow)
                .orElseThrow(() -> new ResponseException("User not found"));

        if (currentUser.equals(userToFollow)) {
            throw new ResponseException("You cannot follow yourself.");
        }

        currentUser.followUser(userToFollow);
        userRepository.save(currentUser);
    }

    public void unfollowUser(Long userIdToUnfollow) {
        AppUser currentUser = getLoggedUser();
        AppUser userToUnfollow = userRepository.findById(userIdToUnfollow)
                .orElseThrow(() -> new ResponseException("User not found"));

        currentUser.unfollowUser(userToUnfollow);
        userRepository.save(currentUser);
    }

    public void updateAppUserByUserRole(Long id, UserRole userRole) {
        userRepository.updateAppUserByUserRole(id, userRole);
    }

    public void blockUser(Long userId, ZonedDateTime date) {
        userRepository.blockUser(userId, date);
    }

    public void unblockUser(Long userId) {
        userRepository.unBlockUser(userId);
    }
}
