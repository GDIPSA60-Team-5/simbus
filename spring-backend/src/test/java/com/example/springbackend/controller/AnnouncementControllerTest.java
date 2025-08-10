package com.example.springbackend.controller;

import com.example.springbackend.dto.request.AnnouncementRequest;
import com.example.springbackend.model.Announcement;
import com.example.springbackend.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(controllers = AnnouncementController.class)
@Import(com.example.springbackend.config.TestSecurityConfig.class) // import your test security config
class AnnouncementControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AnnouncementService announcementService;

    private Announcement sampleAnnouncement;

    @BeforeEach
    void setup() {
        sampleAnnouncement = Announcement.builder()
                .id("ann1")
                .title("Test Announcement")
                .content("This is a test announcement")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .userId("user1")
                .build();
    }

    @Test
    @DisplayName("GET /announcements returns all announcements")
    void testGetAllAnnouncements() {
        when(announcementService.getAllAnnouncements())
                .thenReturn(Flux.just(sampleAnnouncement));

        webTestClient.get()
                .uri("/announcements")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Announcement.class)
                .hasSize(1)
                .contains(sampleAnnouncement);
    }

    @Test
    @DisplayName("GET /announcements/user/{userId} returns announcements by userId")
    void testGetAnnouncementsByUserId() {
        when(announcementService.getAnnouncementsByUserId("user1"))
                .thenReturn(Flux.just(sampleAnnouncement));

        webTestClient.get()
                .uri("/announcements/user/user1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Announcement.class)
                .hasSize(1)
                .contains(sampleAnnouncement);
    }

    @Test
    @DisplayName("POST /announcements creates a new announcement")
    void testCreateAnnouncement() {
        AnnouncementRequest request = new AnnouncementRequest(
                "Test Announcement",
                "This is a test announcement",
                Instant.now().plusSeconds(3600),
                "user1"
        );

        when(announcementService.createAnnouncement(
                request.title(),
                request.content(),
                request.expiresAt(),
                request.userId()))
                .thenReturn(Mono.just(sampleAnnouncement));

        webTestClient.post()
                .uri("/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("ann1")
                .jsonPath("$.title").isEqualTo("Test Announcement");
    }

    @Test
    @DisplayName("DELETE /announcements/{id} deletes an announcement")
    void testDeleteAnnouncement() {
        when(announcementService.deleteAnnouncement("ann1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/announcements/ann1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
