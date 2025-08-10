package com.example.springbackend.controller;

import com.example.springbackend.config.TestSecurityConfig;
import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.repository.CommutePlanRepository;
import com.example.springbackend.repository.UserRepository;
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

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@WebFluxTest(controllers = CommutePlanController.class)
@Import(TestSecurityConfig.class)
class CommutePlanControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CommutePlanRepository commutePlanRepository;

    @MockitoBean
    private UserRepository userRepository;

    private CommutePlan samplePlan;

    @BeforeEach
    void setup() {
        samplePlan = CommutePlan.builder()
                .id("plan1")
                .commutePlanName("My Commute")
                .notifyAt(LocalTime.of(7, 30))
                .arrivalTime(LocalTime.of(8, 0))
                .reminderOffsetMin(15)
                .recurrence(true)
                .startLocationId("start1")
                .endLocationId("end1")
                .userId("user1")
                .commuteHistoryIds(List.of("hist1", "hist2"))
                .preferredRouteIds(List.of("route1"))
                .commuteRecurrenceDayIds(List.of("day1", "day2"))
                .build();
    }

    @Test
    @DisplayName("POST /api/commute-plans creates a new commute plan")
    void testCreateCommutePlan() {
        when(commutePlanRepository.save(ArgumentMatchers.any(CommutePlan.class)))
                .thenReturn(Mono.just(samplePlan));

        webTestClient.post()
                .uri("/api/commute-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(samplePlan)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("plan1")
                .jsonPath("$.commutePlanName").isEqualTo("My Commute");
    }

    @Test
    @DisplayName("GET /api/commute-plans returns all commute plans")
    void testGetAllCommutePlans() {
        when(commutePlanRepository.findAll())
                .thenReturn(Flux.just(samplePlan));

        webTestClient.get()
                .uri("/api/commute-plans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CommutePlan.class)
                .hasSize(1)
                .contains(samplePlan);
    }

    @Test
    @DisplayName("GET /api/commute-plans/{id} returns a commute plan by id")
    void testGetCommutePlanById() {
        when(commutePlanRepository.findById("plan1"))
                .thenReturn(Mono.just(samplePlan));

        webTestClient.get()
                .uri("/api/commute-plans/plan1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("plan1")
                .jsonPath("$.commutePlanName").isEqualTo("My Commute");
    }

    @Test
    @DisplayName("GET /api/commute-plans/{id} returns 404 if not found")
    void testGetCommutePlanByIdNotFound() {
        when(commutePlanRepository.findById("unknown"))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/commute-plans/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /api/commute-plans/{id} updates an existing commute plan")
    void testUpdateCommutePlan() {
        CommutePlan updatedPlan = CommutePlan.builder()
                .commutePlanName("Updated Commute")
                .notifyAt(LocalTime.of(7, 0))
                .arrivalTime(LocalTime.of(8, 15))
                .reminderOffsetMin(10)
                .recurrence(false)
                .startLocationId("start2")
                .endLocationId("end2")
                .userId("user2")
                .commuteHistoryIds(List.of())
                .preferredRouteIds(List.of())
                .commuteRecurrenceDayIds(List.of())
                .build();

        when(commutePlanRepository.findById("plan1"))
                .thenReturn(Mono.just(samplePlan));
        when(commutePlanRepository.save(ArgumentMatchers.any(CommutePlan.class)))
                .thenReturn(Mono.just(updatedPlan));

        webTestClient.put()
                .uri("/api/commute-plans/plan1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPlan)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.commutePlanName").isEqualTo("Updated Commute")
                .jsonPath("$.notifyAt").isEqualTo("07:00:00")
                .jsonPath("$.recurrence").isEqualTo(false);
    }

    @Test
    @DisplayName("PUT /api/commute-plans/{id} returns 404 if plan not found")
    void testUpdateCommutePlanNotFound() {
        when(commutePlanRepository.findById("plan1"))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/commute-plans/plan1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(samplePlan)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /api/commute-plans/{id} deletes a commute plan")
    void testDeleteCommutePlan() {
        when(commutePlanRepository.existsById("plan1"))
                .thenReturn(Mono.just(true));
        when(commutePlanRepository.deleteById("plan1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/commute-plans/plan1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /api/commute-plans/{id} returns 404 if plan does not exist")
    void testDeleteCommutePlanNotFound() {
        when(commutePlanRepository.existsById("plan1"))
                .thenReturn(Mono.just(false));

        webTestClient.delete()
                .uri("/api/commute-plans/plan1")
                .exchange()
                .expectStatus().isNotFound();
    }
}
