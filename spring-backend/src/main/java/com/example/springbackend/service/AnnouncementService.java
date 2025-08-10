package com.example.springbackend.service;

import com.example.springbackend.model.Announcement;
import com.example.springbackend.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    // Get all announcements
    public Flux<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    // Get announcements by user ID
    public Flux<Announcement> getAnnouncementsByUserId(String userId) {
        return announcementRepository.findByUserId(userId);
    }

    // Create new announcement
    public Mono<Announcement> createAnnouncement(String title, String content, Instant expiresAt, String userId) {
        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .userId(userId)
                .build();
        return announcementRepository.save(announcement);
    }

    // Delete announcement by ID
    public Mono<Void> deleteAnnouncement(String id) {
        return announcementRepository.deleteById(id);
    }
}
