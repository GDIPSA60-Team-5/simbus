package com.example.springbackend.service;

import com.example.springbackend.model.UserNotification;
import com.example.springbackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Get all notifications for a user
    public Flux<UserNotification> getUserNotifications(Long userID) {
        return notificationRepository.findByUserId(userID);
    }

    // Send/create a new notification
    public Mono<UserNotification> sendNotification(Long userID, String type, String title, String message, LocalDateTime expiresAt) {
        UserNotification notification = UserNotification.builder()
                .userId(userID)
                .type(type)
                .title(title)
                .message(message)
                .sentAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        return notificationRepository.save(notification);
    }

    // Delete notification by ID
    public Mono<Void> deleteNotification(String notificationId) {
        return notificationRepository.deleteById(notificationId);
    }
}
