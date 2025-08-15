package com.example.springbackend.controller;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.CommutePlanRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/api/user/commute-plans")
public class UserCommutePlanController {

    private final CommutePlanRepository commutePlanRepository;
    private final UserRepository userRepository;

    public UserCommutePlanController(CommutePlanRepository commutePlanRepository,
                                     UserRepository userRepository) {
        this.commutePlanRepository = commutePlanRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new commute plan for the authenticated user
     */
    @PostMapping
    public Mono<ResponseEntity<CommutePlan>> createCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @Valid @RequestBody CommutePlan commutePlan) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> {
                    commutePlan.setUserId(user.getId());
                    return commutePlanRepository.save(commutePlan);
                })
                .map(savedPlan -> ResponseEntity.status(HttpStatus.CREATED).body(savedPlan))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Get all commute plans for the authenticated user
     */
    @GetMapping("/me")
    public Flux<CommutePlan> getMyCommutes(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return getUserFromPrincipal(principal)
                .flatMapMany(user -> commutePlanRepository.findByUserId(user.getId()))
                .onErrorResume(error -> {
                    log.error("Error retrieving commute plans for user", error);
                    return Flux.empty();
                });
    }

    /**
     * Get a specific commute plan by ID (only if it belongs to the authenticated user)
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> getCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId()))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Update a commute plan (only if it belongs to the authenticated user)
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> updateCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id,
            @Valid @RequestBody CommutePlanUpdateRequest updateRequest) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId())
                        .map(existingPlan -> applyUpdates(existingPlan, updateRequest))
                        .flatMap(commutePlanRepository::save))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Delete a commute plan (only if it belongs to the authenticated user)
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId())
                        .flatMap(plan -> commutePlanRepository.delete(plan)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()))))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Helper method to extract user from authentication principal
     */
    private Mono<User> getUserFromPrincipal(Mono<UserDetails> principal) {
        return principal
                .flatMap(userDetails -> userRepository.findByUserName(userDetails.getUsername()))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    /**
     * Helper method to find commute plan by ID and verify ownership
     */
    private Mono<CommutePlan> findCommutePlanByIdAndUserId(String id, String userId) {
        return commutePlanRepository.findById(id)
                .filter(plan -> plan.getUserId().equals(userId))
                .switchIfEmpty(Mono.empty()); // Will result in 404 response
    }

    /**
     * Update record for partial updates
     */
    public record CommutePlanUpdateRequest(
            String commutePlanName,
            String notifyAt,
            LocalTime arrivalTime,
            Integer reminderOffsetMin,
            Boolean recurrence,
            String startLocationId,
            String endLocationId,
            String savedTripRouteId,
            java.util.List<String> commuteRecurrenceDayIds
    ) {}

    /**
     * Helper method to create updated commute plan using builder pattern or copy constructor
     */
    private CommutePlan applyUpdates(CommutePlan existingPlan, CommutePlanUpdateRequest updates) {
        return CommutePlan.builder()
                .id(existingPlan.getId())
                .userId(existingPlan.getUserId())
                .commutePlanName(updates.commutePlanName() != null ?
                        updates.commutePlanName() : existingPlan.getCommutePlanName())
                .notifyAt(updates.notifyAt() != null ?
                        updates.notifyAt() : existingPlan.getNotifyAt())
                .arrivalTime(updates.arrivalTime() != null ?
                        updates.arrivalTime() : existingPlan.getArrivalTime())
                .reminderOffsetMin(updates.reminderOffsetMin() != null ?
                        updates.reminderOffsetMin() : existingPlan.getReminderOffsetMin())
                .recurrence(updates.recurrence() != null ?
                        updates.recurrence() : existingPlan.getRecurrence())
                .startLocationId(updates.startLocationId() != null ?
                        updates.startLocationId() : existingPlan.getStartLocationId())
                .endLocationId(updates.endLocationId() != null ?
                        updates.endLocationId() : existingPlan.getEndLocationId())
                .savedTripRouteId(updates.savedTripRouteId() != null ?
                        updates.savedTripRouteId() : existingPlan.getSavedTripRouteId())
                .commuteRecurrenceDayIds(updates.commuteRecurrenceDayIds() != null ?
                        updates.commuteRecurrenceDayIds() : existingPlan.getCommuteRecurrenceDayIds())
                .build();
    }
}