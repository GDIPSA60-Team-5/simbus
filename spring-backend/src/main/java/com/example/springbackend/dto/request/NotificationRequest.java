package com.example.springbackend.dto.request;

import java.time.LocalDateTime;

public record NotificationRequest(
        Long userID,
        String type,
        String title,
        String message,
        LocalDateTime expiresAt
) {}
