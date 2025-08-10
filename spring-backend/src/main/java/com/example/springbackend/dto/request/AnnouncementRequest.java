package com.example.springbackend.dto.request;

import java.time.Instant;

public record AnnouncementRequest(
        String title,
        String content,
        Instant expiresAt,
        String userId
) {}
