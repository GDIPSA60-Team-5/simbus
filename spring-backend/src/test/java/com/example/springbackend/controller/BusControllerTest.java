package com.example.springbackend.controller;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.service.implementation.BusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(BusController.class)
@ContextConfiguration(classes = BusController.class)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class
})
class BusControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BusService busService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Mock initDatabase dependencies from SpringBackendApplication
        when(userRepository.findByUserName(anyString()))
                .thenReturn(Mono.empty());
        when(userRepository.save(any()))
                .thenReturn(Mono.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");
    }

    @Test
    @DisplayName("GET /api/bus/stops/search returns matching stops")
    void testSearchBusStops() {
        BusStop stop1 = new BusStop("UTOWN", "University Town", 1.234, 103.456, "NUS");
        BusStop stop2 = new BusStop("COM2", "Computing 2", 1.236, 103.458, "NUS");

        when(busService.searchBusStops("Utown"))
                .thenReturn(Flux.just(stop1, stop2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/bus/stops/search")
                        .queryParam("query", "Utown")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BusStop.class)
                .contains(stop1, stop2);
    }

    @Test
    @DisplayName("POST /api/bus/arrivals returns arrivals for a stop")
    void testGetBusArrivalsForStop() {
        BusStop stop = new BusStop("UTOWN", "University Town", 1.234, 103.456, "NUS");

        ZonedDateTime fixedDateTime = ZonedDateTime.of(
                2025, 8, 11, 1, 20, 21, 0, ZoneId.of("Asia/Singapore")
        );

        BusArrival expectedArrival = new BusArrival("96", "NUS", List.of(fixedDateTime));

        when(busService.getArrivalsForStop(stop))
                .thenReturn(Flux.just(expectedArrival));

        webTestClient.post()
                .uri("/api/bus/arrivals")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(stop)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BusArrival.class)
                .value(arrivals -> {
                    org.assertj.core.api.Assertions.assertThat(arrivals).hasSize(1);

                    BusArrival actual = arrivals.get(0);

                    // Compare simple fields
                    org.assertj.core.api.Assertions.assertThat(actual.serviceName()).isEqualTo(expectedArrival.serviceName());
                    org.assertj.core.api.Assertions.assertThat(actual.operator()).isEqualTo(expectedArrival.operator());

                    // Check arrivals list size
                    org.assertj.core.api.Assertions.assertThat(actual.arrivals()).hasSize(1);

                    // Compare timestamps using toInstant() to avoid zone/nano issues
                    org.assertj.core.api.Assertions.assertThat(actual.arrivals().get(0).toInstant())
                            .isEqualTo(expectedArrival.arrivals().get(0).toInstant());
                });
    }

    @Test
    @DisplayName("GET /api/bus/arrivals filters by serviceNo if provided")
    void testGetArrivalsForStopAndService() {
        BusStop stop = new BusStop("UTOWN", "University Town", 1.234, 103.456, "NUS");

        ZonedDateTime fixedDateTime = ZonedDateTime.of(
                2025, 8, 11, 1, 20, 21, 0, ZoneId.of("Asia/Singapore")
        );

        BusArrival arrival96 = new BusArrival("96", "NUS", List.of(fixedDateTime));
        BusArrival arrival151 = new BusArrival("151", "NUS", List.of(fixedDateTime));

        when(busService.searchBusStops("Utown"))
                .thenReturn(Flux.just(stop));
        when(busService.getArrivalsForStop(stop))
                .thenReturn(Flux.just(arrival96, arrival151));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/bus/arrivals")
                        .queryParam("busStopQuery", "Utown")
                        .queryParam("serviceNo", "96")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BusArrival.class)
                .value(arrivals -> {
                    org.assertj.core.api.Assertions.assertThat(arrivals).hasSize(1);

                    BusArrival actual = arrivals.get(0);

                    // Compare simple fields
                    org.assertj.core.api.Assertions.assertThat(actual.serviceName()).isEqualTo("96");
                    org.assertj.core.api.Assertions.assertThat(actual.operator()).isEqualTo("NUS");

                    // Check arrivals list size
                    org.assertj.core.api.Assertions.assertThat(actual.arrivals()).hasSize(1);

                    // Compare timestamps with toInstant()
                    org.assertj.core.api.Assertions.assertThat(actual.arrivals().get(0).toInstant())
                            .isEqualTo(fixedDateTime.toInstant());
                });
    }
}
