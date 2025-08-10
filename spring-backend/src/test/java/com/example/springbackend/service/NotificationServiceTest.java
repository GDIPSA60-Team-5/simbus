package com.example.springbackend.service;

import com.example.springbackend.model.UserNotification;
import com.example.springbackend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserNotifications() {
        UserNotification notification1 = UserNotification.builder()
                .userId("user1")
                .type("info")
                .title("Title 1")
                .message("Message 1")
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        UserNotification notification2 = UserNotification.builder()
                .userId("user1")
                .type("alert")
                .title("Title 2")
                .message("Message 2")
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        when(notificationRepository.findByUserId("user1"))
                .thenReturn(Flux.just(notification1, notification2));

        StepVerifier.create(notificationService.getUserNotifications("user1"))
                .expectNext(notification1)
                .expectNext(notification2)
                .verifyComplete();

        verify(notificationRepository, times(1)).findByUserId("user1");
    }

    @Test
    void testSendNotification() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        UserNotification savedNotification = UserNotification.builder()
                .userId("user2")
                .type("info")
                .title("Test Title")
                .message("Test Message")
                .sentAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        when(notificationRepository.save(any(UserNotification.class)))
                .thenReturn(Mono.just(savedNotification));

        StepVerifier.create(notificationService.sendNotification(
                        "user2", "info", "Test Title", "Test Message", expiresAt))
                .expectNextMatches(notification ->
                        notification.getUserId().equals("user2") &&
                                notification.getType().equals("info") &&
                                notification.getTitle().equals("Test Title") &&
                                notification.getMessage().equals("Test Message") &&
                                notification.getExpiresAt().equals(expiresAt))
                .verifyComplete();

        verify(notificationRepository, times(1)).save(any(UserNotification.class));
    }

    @Test
    void testDeleteNotification() {
        when(notificationRepository.deleteById("notif123")).thenReturn(Mono.empty());

        StepVerifier.create(notificationService.deleteNotification("notif123"))
                .verifyComplete();

        verify(notificationRepository, times(1)).deleteById("notif123");
    }
}
