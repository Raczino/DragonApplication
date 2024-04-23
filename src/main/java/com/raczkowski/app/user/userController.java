package com.raczkowski.app.user;

import com.raczkowski.app.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class userController {

    private final UserService userService;

    @GetMapping("/get")
    ResponseEntity<UserDto> getUserById(@RequestParam Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/get/login")
    ResponseEntity<AppUser> getLoggedUser(){
        return ResponseEntity.ok(userService.getLoggedUser());
    }
}
