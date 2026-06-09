package com.raczkowski.app.user;

import com.raczkowski.app.accountPremium.service.SubscriptionService;
import com.raczkowski.app.common.offset.SliceResponse;
import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.dto.UserDtoAssembler;
import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/get/logged")
    ResponseEntity<AppUser> getLoggedUser() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @PostMapping("/{userId}/follow")
    public void followUser(@PathVariable Long userId) {
        userService.followUser(userId);
    }

    @GetMapping("/is-following/{userId}")
    ResponseEntity<Boolean> isUserFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.isUserFollowingProvidedUser(userId));
    }

    @PostMapping("/{userId}/unfollow")
    public void unfollowUser(@PathVariable Long userId) {
        userService.unfollowUser(userId);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<SliceResponse<UserDto>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(userService.getFollowersListForUser(userId, offset, limit));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<SliceResponse<UserDto>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(userService.getFollowingUsersPerUser(userId, offset, limit));
    }

    @PostMapping("/save")
    public void createSubscriptionForUser(@RequestParam Long id, @RequestParam PremiumAccountRange type) {
        subscriptionService.createSubscriptionForUser(id, type);
    }

    @PostMapping("/activate/subscription")
    public void activate(@RequestParam Long id) {
        subscriptionService.activateSubscription(id);
    }
}
