package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class UserNotificationTest {

    @Test
    public void testUserNotificationBuilderAndGetters() {
        String id = "notif123";
        String userId = "user456";
        String type = "INFO";
        String title = "Reminder";
        String message = "Your subscription is about to expire.";
        LocalDateTime sentAt = LocalDateTime.of(2025, 8, 10, 12, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2025, 8, 17, 12, 0);

        UserNotification notification = UserNotification.builder()
                .id(id)
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .sentAt(sentAt)
                .expiresAt(expiresAt)
                .build();

        assertEquals(id, notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(type, notification.getType());
        assertEquals(title, notification.getTitle());
        assertEquals(message, notification.getMessage());
        assertEquals(sentAt, notification.getSentAt());
        assertEquals(expiresAt, notification.getExpiresAt());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        UserNotification notification = new UserNotification();

        notification.setId("notif789");
        notification.setUserId("user999");
        notification.setType("ALERT");
        notification.setTitle("Urgent Update");
        notification.setMessage("Please update your profile information.");
        notification.setSentAt(LocalDateTime.of(2025, 8, 11, 9, 30));
        notification.setExpiresAt(LocalDateTime.of(2025, 8, 18, 9, 30));

        assertEquals("notif789", notification.getId());
        assertEquals("user999", notification.getUserId());
        assertEquals("ALERT", notification.getType());
        assertEquals("Urgent Update", notification.getTitle());
        assertEquals("Please update your profile information.", notification.getMessage());
        assertEquals(LocalDateTime.of(2025, 8, 11, 9, 30), notification.getSentAt());
        assertEquals(LocalDateTime.of(2025, 8, 18, 9, 30), notification.getExpiresAt());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        UserNotification notif1 = UserNotification.builder()
                .id("notif123")
                .userId("user456")
                .type("INFO")
                .title("Reminder")
                .message("Your subscription is about to expire.")
                .sentAt(LocalDateTime.of(2025, 8, 10, 12, 0))
                .expiresAt(LocalDateTime.of(2025, 8, 17, 12, 0))
                .build();

        UserNotification notif2 = UserNotification.builder()
                .id("notif123")
                .userId("user456")
                .type("INFO")
                .title("Reminder")
                .message("Your subscription is about to expire.")
                .sentAt(LocalDateTime.of(2025, 8, 10, 12, 0))
                .expiresAt(LocalDateTime.of(2025, 8, 17, 12, 0))
                .build();

        UserNotification notif3 = UserNotification.builder()
                .id("notif999")
                .userId("user999")
                .type("ALERT")
                .title("Urgent Update")
                .message("Please update your profile information.")
                .sentAt(LocalDateTime.of(2025, 8, 11, 9, 30))
                .expiresAt(LocalDateTime.of(2025, 8, 18, 9, 30))
                .build();

        // equals and hashCode positive
        assertEquals(notif1, notif2);
        assertEquals(notif1.hashCode(), notif2.hashCode());

        // equals and hashCode negative
        assertNotEquals(notif1, notif3);
        assertNotEquals(notif1.hashCode(), notif3.hashCode());

        // equals with null and different class
        assertNotEquals(null, notif1);
        assertNotEquals("some string", notif1);

        // toString test contains key info
        String toString = notif1.toString();
        assertTrue(toString.contains("notif123"));
        assertTrue(toString.contains("user456"));
        assertTrue(toString.contains("INFO"));
        assertTrue(toString.contains("Reminder"));
    }
}
