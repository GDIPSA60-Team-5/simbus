package com.example.springbackend.controller;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.repository.CommutePlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/admin/commute-plans")
@PreAuthorize("hasRole('ADMIN')")
public class CommutePlanController {

    private final CommutePlanRepository commutePlanRepository;

    public CommutePlanController(CommutePlanRepository commutePlanRepository) {
        this.commutePlanRepository = commutePlanRepository;
    }

    /**
     * Get all commute plans (admin only)
     */
    @GetMapping
    public Flux<CommutePlan> getAllCommutePlans() {
        return commutePlanRepository.findAll()
                .onErrorResume(error -> {
                    log.error("Error retrieving all commute plans", error);
                    return Flux.empty();
                });
    }

    /**
     * Get commute plans by user ID (admin only)
     */
    @GetMapping("/user/{userId}")
    public Flux<CommutePlan> getCommutePlansByUserId(@PathVariable String userId) {
        return commutePlanRepository.findByUserId(userId)
                .onErrorResume(error -> {
                    log.error("Error retrieving commute plans for user: {}", userId, error);
                    return Flux.empty();
                });
    }

    /**
     * Get a specific commute plan by ID (admin only)
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> getCommutePlan(@PathVariable String id) {
        return commutePlanRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Delete a commute plan by ID (admin only)
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCommutePlan(@PathVariable String id) {
        return commutePlanRepository.findById(id)
                .flatMap(plan -> commutePlanRepository.delete(plan)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Update a commute plan by ID (admin only)
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> updateCommutePlan(
            @PathVariable String id,
            @Valid @RequestBody CommutePlan updatedPlan) {
        
        return commutePlanRepository.findById(id)
                .flatMap(existingPlan -> {
                    updatedPlan.setId(id);
                    return commutePlanRepository.save(updatedPlan);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Get commute plans count by user (admin analytics)
     */
    @GetMapping("/analytics/count-by-user")
    public Mono<ResponseEntity<Long>> getCommutePlansCount() {
        return commutePlanRepository.count()
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}