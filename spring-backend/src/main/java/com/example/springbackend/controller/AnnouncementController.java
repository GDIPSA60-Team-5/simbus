package com.example.springbackend.controller;

import com.example.springbackend.model.Announcement;
import com.example.springbackend.service.AnnouncementService;
import com.example.springbackend.dto.request.AnnouncementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    // demo
    private final AnnouncementService announcementService;

    // Get all announcements
    @GetMapping
    public Flux<Announcement> getAllAnnouncements() {
        return announcementService.getAllAnnouncements();
    }

    // Get announcements by userId
    @GetMapping("/user/{userId}")
    public Flux<Announcement> getAnnouncementsByUserId(@PathVariable String userId) {
        return announcementService.getAnnouncementsByUserId(userId);
    }

    // Create new announcement
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Announcement> createAnnouncement(@RequestBody AnnouncementRequest request) {
        return announcementService.createAnnouncement(
                request.title(),
                request.content(),
                request.expiresAt(),
                request.userId()
        );
    }

    // Delete announcement by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAnnouncement(@PathVariable String id) {
        return announcementService.deleteAnnouncement(id);
    }
}
