package com.raczkowski.app.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoSender sender;

    @PostMapping("/send")
    public String send(@RequestParam String msg) {
        sender.send(msg);
        return "Sent: " + msg;
    }
}

