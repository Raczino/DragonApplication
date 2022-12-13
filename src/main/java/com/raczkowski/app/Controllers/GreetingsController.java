package com.raczkowski.app.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/greetings")
public class GreetingsController {

    @GetMapping
    public ResponseEntity<String> sayHello(){
        return ResponseEntity.ok("Hello from api");
    }

    @GetMapping("/bye")
    public ResponseEntity<String> sayBye(){
        return ResponseEntity.ok("bye bye");
    }
}
