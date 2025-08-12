package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.Instant;

@Document("announcements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {

    @Id
    private String id;

    private String title;
    private String content;
    private Instant createdAt;
    private Instant expiresAt;

    private Long userId;  // who created or made this announcement
}
