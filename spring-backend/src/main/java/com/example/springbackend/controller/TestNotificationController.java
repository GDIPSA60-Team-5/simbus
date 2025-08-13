package com.example.springbackend.controller;

import com.example.springbackend.service.FcmService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/test-fcm")
public class TestNotificationController {

    private final FcmService fcmService;

    public TestNotificationController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/send-notification")
    public Mono<String> sendNotification(@RequestParam String fcmToken) {
        fcmService.sendNotification(fcmToken, Map.of(
                "title", "Test Notification",
                "body", "Hello from backend!"
        ));
        return Mono.just("Notification sent (or attempted)");
    }
}
