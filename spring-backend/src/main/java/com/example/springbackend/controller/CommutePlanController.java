package com.example.springbackend.controller;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.repository.CommutePlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/commute-plans")
public class CommutePlanController {

    private final CommutePlanRepository commutePlanRepository;

    public CommutePlanController(CommutePlanRepository commutePlanRepository) {
        this.commutePlanRepository = commutePlanRepository;
    }

    // CREATE
    @PostMapping
    public Mono<CommutePlan> createCommutePlan(@RequestBody CommutePlan commutePlan) {
        return commutePlanRepository.save(commutePlan);
    }

    // READ ALL
    @GetMapping
    public Flux<CommutePlan> getAllCommutePlans() {
        return commutePlanRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> getCommutePlanById(@PathVariable String id) {
        return commutePlanRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommutePlan>> updateCommutePlan(
            @PathVariable String id,
            @RequestBody CommutePlan updatedPlan) {

        return commutePlanRepository.findById(id)
                .flatMap(existingPlan -> {
                    existingPlan.setCommutePlanName(updatedPlan.getCommutePlanName());
                    existingPlan.setNotifyAt(updatedPlan.getNotifyAt());
                    existingPlan.setArrivalTime(updatedPlan.getArrivalTime());
                    existingPlan.setReminderOffsetMin(updatedPlan.getReminderOffsetMin());
                    existingPlan.setRecurrence(updatedPlan.getRecurrence());
                    existingPlan.setStartLocationId(updatedPlan.getStartLocationId());
                    existingPlan.setEndLocationId(updatedPlan.getEndLocationId());
                    existingPlan.setUserId(updatedPlan.getUserId());
                    // handle collections carefully (ids or embedded docs)

                    return commutePlanRepository.save(existingPlan);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCommutePlan(@PathVariable String id) {
        return commutePlanRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return commutePlanRepository.deleteById(id)
                                .then(Mono.just(ResponseEntity.noContent().build()));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }
}
