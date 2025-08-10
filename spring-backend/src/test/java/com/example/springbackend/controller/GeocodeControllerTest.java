package com.example.springbackend.controller;

import com.example.springbackend.service.GeocodingService;
import com.example.springbackend.service.ReverseGeocodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(controllers = GeocodeController.class)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class
})
public class GeocodeControllerTest {

    @MockitoBean
    private GeocodingService geocodingService;

    @MockitoBean
    private ReverseGeocodeService reverseGeocodeService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /api/geocode returns list of candidates")
    void testGeocodeReturnsCandidates() {
        var candidate = new GeocodeController.GeocodeCandidate(
                "1.234", "103.456", "Orchard Rd", "238823", "12", "Orchard Rd", "Some Building"
        );

        when(geocodingService.getCandidates("Orchard"))
                .thenReturn(Mono.just(List.of(candidate)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/geocode")
                        .queryParam("locationName", "Orchard")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.found").isEqualTo(1)
                .jsonPath("$.results[0].latitude").isEqualTo("1.234")
                .jsonPath("$.results[0].displayName").isEqualTo("Orchard Rd");
    }

    @Test
    @DisplayName("GET /api/geocode returns 404 if no candidates")
    void testGeocodeReturnsNotFoundIfEmpty() {
        when(geocodingService.getCandidates(anyString()))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/geocode")
                        .queryParam("locationName", "Nowhere")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /api/reverse-geocode returns list of candidates")
    void testReverseGeocodeReturnsCandidates() {
        var candidate = new GeocodeController.ReverseGeocodeCandidate("Some Address, Singapore 123456");

        when(reverseGeocodeService.getCandidates("24291.9778", "31373.0117"))
                .thenReturn(Mono.just(List.of(candidate)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reverse-geocode")
                        .queryParam("x", "24291.9778")
                        .queryParam("y", "31373.0117")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.candidates[0].address").isEqualTo("Some Address, Singapore 123456");
    }

    @Test
    @DisplayName("GET /api/reverse-geocode returns 404 if no candidates")
    void testReverseGeocodeReturnsNotFoundIfEmpty() {
        when(reverseGeocodeService.getCandidates(anyString(), anyString()))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reverse-geocode")
                        .queryParam("x", "0")
                        .queryParam("y", "0")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}

