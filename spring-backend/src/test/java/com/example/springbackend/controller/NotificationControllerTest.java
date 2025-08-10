package com.example.springbackend.controller;

import com.example.springbackend.config.TestSecurityConfig;
import com.example.springbackend.dto.request.NotificationRequest;
import com.example.springbackend.model.UserNotification;
import com.example.springbackend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(controllers = NotificationController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private NotificationService notificationService;

    private UserNotification sampleNotification;
    private NotificationRequest sampleRequest;

    @BeforeEach
    void setup() {
        sampleNotification = UserNotification.builder()
                .id("notif1")
                .userId("user1")
                .type("INFO")
                .title("Test Notification")
                .message("This is a test message")
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        sampleRequest = new NotificationRequest(
                "user1",
                "INFO",
                "Test Notification",
                "This is a test message",
                LocalDateTime.now().plusDays(1)
        );
    }

    @Test
    @DisplayName("GET /notifications/user/{userID} returns notifications for a user")
    void testGetUserNotifications() {
        when(notificationService.getUserNotifications("user1"))
                .thenReturn(Flux.just(sampleNotification));

        webTestClient.get()
                .uri("/notifications/user/user1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserNotification.class)
                .hasSize(1)
                .contains(sampleNotification);
    }

    @Test
    @DisplayName("POST /notifications creates a new notification")
    void testSendNotification() {
        when(notificationService.sendNotification(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(Mono.just(sampleNotification));

        webTestClient.post()
                .uri("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("notif1")
                .jsonPath("$.userId").isEqualTo("user1")
                .jsonPath("$.type").isEqualTo("INFO")
                .jsonPath("$.title").isEqualTo("Test Notification")
                .jsonPath("$.message").isEqualTo("This is a test message");
    }

    @Test
    @DisplayName("DELETE /notifications/{id} deletes a notification")
    void testDeleteNotification() {
        when(notificationService.deleteNotification("notif1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/notifications/notif1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
