package com.raczkowski.app.user;

import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
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

    public void checkIfEmailExists(String email) {
        if (userRepository.findByEmail(email) != null) {
            throw new ResponseException(ErrorMessages.USER_ALREADY_EXITS);
        }
    }

    public String signUpUser(AppUser user) {
        String encodedPassword = bCryptPasswordEncoder
                .encode(user.getPassword());

        user.setPassword(encodedPassword);

        userRepository.save(user);

        return null;
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
                .orElseThrow(() -> new ResponseException(ErrorMessages.USER_NOT_FOUND));

        if (currentUser.equals(userToFollow)) {
            throw new ResponseException(ErrorMessages.CANNOT_FOLLOW_YOURSELF);
        }

        currentUser.followUser(userToFollow);
        userRepository.save(currentUser);
    }

    public void unfollowUser(Long userIdToUnfollow) {
        AppUser currentUser = getLoggedUser();
        AppUser userToUnfollow = userRepository.findById(userIdToUnfollow)
                .orElseThrow(() -> new ResponseException(ErrorMessages.USER_NOT_FOUND));

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
