package com.example.springbackend.controller;

import com.example.springbackend.config.TestSecurityConfig;
import com.example.springbackend.model.FavoriteLocation;
import com.example.springbackend.service.FavoriteLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(controllers = FavoriteLocationController.class)
@Import(TestSecurityConfig.class) // bypass security for tests
class FavoriteLocationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FavoriteLocationService favoriteLocationService;

    private FavoriteLocation sampleLocation;

    @BeforeEach
    void setup() {
        sampleLocation = FavoriteLocation.builder()
                .id("loc1")
                .userId("user1")
                .locationName("Home")
                .latitude(1.2345)
                .longitude(5.6789)
                .build();
    }

    @Test
    @DisplayName("GET /favorite-locations/user/{userId} returns favorite locations by userId")
    void testGetLocationsByUserId() {
        when(favoriteLocationService.getLocationsByUserId("user1"))
                .thenReturn(Flux.just(sampleLocation));

        webTestClient.get()
                .uri("/favorite-locations/user/user1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FavoriteLocation.class)
                .hasSize(1)
                .contains(sampleLocation);
    }

    @Test
    @DisplayName("GET /favorite-locations/{id} returns favorite location by id")
    void testGetLocationById() {
        when(favoriteLocationService.getLocationById("loc1"))
                .thenReturn(Mono.just(sampleLocation));

        webTestClient.get()
                .uri("/favorite-locations/loc1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("loc1")
                .jsonPath("$.locationName").isEqualTo("Home")
                .jsonPath("$.latitude").isEqualTo(1.2345)
                .jsonPath("$.longitude").isEqualTo(5.6789);
    }

    @Test
    @DisplayName("POST /favorite-locations creates a new favorite location")
    void testCreateLocation() {
        when(favoriteLocationService.createLocation(ArgumentMatchers.any(FavoriteLocation.class)))
                .thenReturn(Mono.just(sampleLocation));

        webTestClient.post()
                .uri("/favorite-locations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleLocation)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("loc1")
                .jsonPath("$.locationName").isEqualTo("Home");
    }

    @Test
    @DisplayName("PUT /favorite-locations/{id} updates an existing favorite location")
    void testUpdateLocation() {
        FavoriteLocation updatedLocation = FavoriteLocation.builder()
                .id("loc1")
                .userId("user1")
                .locationName("Work")
                .latitude(2.3456)
                .longitude(6.7890)
                .build();

        when(favoriteLocationService.updateLocation("loc1", updatedLocation))
                .thenReturn(Mono.just(updatedLocation));

        webTestClient.put()
                .uri("/favorite-locations/loc1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedLocation)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.locationName").isEqualTo("Work")
                .jsonPath("$.latitude").isEqualTo(2.3456)
                .jsonPath("$.longitude").isEqualTo(6.7890);
    }

    @Test
    @DisplayName("DELETE /favorite-locations/{id} deletes a favorite location")
    void testDeleteLocation() {
        when(favoriteLocationService.deleteLocation("loc1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/favorite-locations/loc1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
