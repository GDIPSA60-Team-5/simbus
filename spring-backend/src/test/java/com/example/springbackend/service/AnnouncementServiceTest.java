package com.example.springbackend.service;

import com.example.springbackend.model.Announcement;
import com.example.springbackend.repository.AnnouncementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    AnnouncementRepository announcementRepository;

    AnnouncementService announcementService;

    @BeforeEach
    void setup() {
        announcementService = new AnnouncementService(announcementRepository);
    }

    @Test
    void testGetAllAnnouncements() {
        Announcement a1 = new Announcement("1", "Title1", "Content1", Instant.now(), Instant.now().plusSeconds(3600), "user1");
        Announcement a2 = new Announcement("2", "Title2", "Content2", Instant.now(), Instant.now().plusSeconds(7200), "user2");

        when(announcementRepository.findAll()).thenReturn(Flux.just(a1, a2));

        StepVerifier.create(announcementService.getAllAnnouncements())
                .expectNext(a1)
                .expectNext(a2)
                .verifyComplete();

        verify(announcementRepository, times(1)).findAll();
    }

    @Test
    void testGetAnnouncementsByUserId() {
        String userId = "user1";
        Announcement a1 = new Announcement("1", "Title1", "Content1", Instant.now(), Instant.now().plusSeconds(3600), userId);

        when(announcementRepository.findByUserId(userId)).thenReturn(Flux.just(a1));

        StepVerifier.create(announcementService.getAnnouncementsByUserId(userId))
                .expectNext(a1)
                .verifyComplete();

        verify(announcementRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testCreateAnnouncement() {
        String title = "New Announcement";
        String content = "Some content";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String userId = "user1";

        Announcement saved = new Announcement("123", title, content, Instant.now(), expiresAt, userId);

        when(announcementRepository.save(any(Announcement.class))).thenReturn(Mono.just(saved));

        Mono<Announcement> result = announcementService.createAnnouncement(title, content, expiresAt, userId);

        StepVerifier.create(result)
                .expectNextMatches(announcement -> announcement.getId().equals("123")
                        && announcement.getTitle().equals(title)
                        && announcement.getUserId().equals(userId))
                .verifyComplete();

        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    @Test
    void testDeleteAnnouncement() {
        String id = "123";
        when(announcementRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(announcementService.deleteAnnouncement(id))
                .verifyComplete();

        verify(announcementRepository, times(1)).deleteById(id);
    }
}
