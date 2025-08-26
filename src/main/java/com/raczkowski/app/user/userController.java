package com.raczkowski.app.user;

import com.raczkowski.app.accountPremium.service.SubscriptionService;
import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.dto.UserDtoAssembler;
import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class userController {

    private final UserService userService;
    private final UserDtoAssembler userDtoAssembler;
    private final SubscriptionService subscriptionService;

    @GetMapping("/get")
    ResponseEntity<UserDto> getUserById(@RequestParam Long id) {
        AppUser user = userService.getUserById(id);
        return ResponseEntity.ok(userDtoAssembler.assemble(user));
    }

    @GetMapping("/get/login")
    ResponseEntity<AppUser> getLoggedUser() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @PostMapping("/{userId}/follow")
    public void followUser(@PathVariable Long userId) {
        userService.followUser(userId);
    }

    @PostMapping("/{userId}/unfollow")
    public void unfollowUser(@PathVariable Long userId) {
        userService.unfollowUser(userId);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserDto>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowersCount(userId).stream()
                .map(userDtoAssembler::assemble).toList());
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserDto>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowingUsersByUserCount(userId).stream()
                .map(userDtoAssembler::assemble).toList());
    }

    @PostMapping("/save")
    public void createSub(@RequestParam Long id, @RequestParam PremiumAccountRange type) {
        subscriptionService.create(id, type);
    }

    @PostMapping("/activate")
    public void activate(@RequestParam Long id) {
        subscriptionService.activateSubscription(id);
    }
}
