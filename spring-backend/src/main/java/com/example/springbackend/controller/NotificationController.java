package com.example.springbackend.controller;

import com.example.springbackend.dto.request.NotificationRequest;
import com.example.springbackend.model.UserNotification;
import com.example.springbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for a user
    @GetMapping("/user/{userID}")
    public Flux<UserNotification> getUserNotifications(@PathVariable Long userID) {
        return notificationService.getUserNotifications(userID);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserNotification> sendNotification(@RequestBody NotificationRequest request) {
        return notificationService.sendNotification(
                request.userID(),
                request.type(),
                request.title(),
                request.message(),
                request.expiresAt()
        );
    }

    // Delete a notification by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteNotification(@PathVariable String id) {
        return notificationService.deleteNotification(id);
    }
}
