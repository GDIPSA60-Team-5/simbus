package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.dto.llm.RoutingIntentDTO;
import com.example.springbackend.dto.llm.DirectionsResponseDTO.LegDTO;
import com.example.springbackend.dto.llm.DirectionsResponseDTO.RouteDTO;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.service.RoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(RoutingController.class)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class
})
class RoutingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RoutingService routingService;

    @MockitoBean
    private UserRepository userRepository; // satisfies initDatabase()

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(userRepository.findByUserName(anyString()))
                .thenReturn(Mono.empty()); // ✅ safe for .switchIfEmpty()

        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(User.builder()
                        .userName("user")
                        .userType("admin")
                        .passwordHash("encoded")
                        .build()));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");
    }

    @Test
    @DisplayName("Should return directions with provided start/end location names")
    void testGetDirectionsWithProvidedLocations() {
        // Arrange mock response
        List<LegDTO> legs = List.of(
                new LegDTO("bus", 15, "123", "Take bus 123", "encodedPolylineData")
        );
        List<RouteDTO> routes = List.of(
                new RouteDTO(15, legs, "Bus 123 to destination")
        );

        when(routingService.getBusRoutes(anyString(), anyString(), any()))
                .thenReturn(Mono.just(routes));

        RoutingIntentDTO requestDto = new RoutingIntentDTO(
                "1.0,2.0",
                "3.0,4.0",
                "Custom Origin",
                "Custom Destination",
                LocalTime.of(9, 30)
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/routing")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DirectionsResponseDTO.class)
                .value(response -> {
                    // Validate mapping
                    org.assertj.core.api.Assertions.assertThat(response.startLocation()).isEqualTo("Custom Origin");
                    org.assertj.core.api.Assertions.assertThat(response.endLocation()).isEqualTo("Custom Destination");
                    org.assertj.core.api.Assertions.assertThat(response.startCoordinates())
                            .isEqualTo(new Coordinates(1.0, 2.0));
                    org.assertj.core.api.Assertions.assertThat(response.endCoordinates())
                            .isEqualTo(new Coordinates(3.0, 4.0));
                    org.assertj.core.api.Assertions.assertThat(response.suggestedRoutes()).hasSize(1);
                });
    }

    @Test
    @DisplayName("Should return default Origin/Destination when names are null or blank")
    void testGetDirectionsWithDefaultLocations() {
        // Arrange mock response
        List<RouteDTO> routes = List.of(
                new RouteDTO(10, List.of(), "No legs")
        );

        when(routingService.getBusRoutes(anyString(), anyString(), any()))
                .thenReturn(Mono.just(routes));

        // startLocation and endLocation are blank -> should default
        RoutingIntentDTO requestDto = new RoutingIntentDTO(
                "5.0,6.0",
                "7.0,8.0",
                "   ",   // blank
                null,   // null
                LocalTime.of(12, 0)
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/routing")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DirectionsResponseDTO.class)
                .value(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.startLocation()).isEqualTo("Origin");
                    org.assertj.core.api.Assertions.assertThat(response.endLocation()).isEqualTo("Destination");
                    org.assertj.core.api.Assertions.assertThat(response.startCoordinates())
                            .isEqualTo(new Coordinates(5.0, 6.0));
                    org.assertj.core.api.Assertions.assertThat(response.endCoordinates())
                            .isEqualTo(new Coordinates(7.0, 8.0));
                });
    }
}
