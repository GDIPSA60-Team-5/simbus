package com.example.springbackend.controller;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.repository.CommutePlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/commute-plans")
public class CommutePlanController {

    private final CommutePlanRepository commutePlanRepository;

    public CommutePlanController(CommutePlanRepository commutePlanRepository) {
        this.commutePlanRepository = commutePlanRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<CommutePlan> createCommutePlan(@RequestBody CommutePlan commutePlan) {
        CommutePlan saved = commutePlanRepository.save(commutePlan);
        return ResponseEntity.ok(saved);
    }

    // READ ALL
    @GetMapping
    public List<CommutePlan> getAllCommutePlans() {
        return commutePlanRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<CommutePlan> getCommutePlanById(@PathVariable Long id) {
        Optional<CommutePlan> optionalPlan = commutePlanRepository.findById(id);
        return optionalPlan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CommutePlan> updateCommutePlan(
            @PathVariable Long id,
            @RequestBody CommutePlan updatedPlan) {

        return commutePlanRepository.findById(id)
                .map(existingPlan -> {
                    // Update fields - example for main fields, add more if needed
                    existingPlan.setCommutePlanName(updatedPlan.getCommutePlanName());
                    existingPlan.setNotifyAt(updatedPlan.getNotifyAt());
                    existingPlan.setArrivalTime(updatedPlan.getArrivalTime());
                    existingPlan.setReminderOffsetMin(updatedPlan.getReminderOffsetMin());
                    existingPlan.setRecurrence(updatedPlan.getRecurrence());
                    existingPlan.setStartLocation(updatedPlan.getStartLocation());
                    existingPlan.setEndLocation(updatedPlan.getEndLocation());
                    existingPlan.setUser(updatedPlan.getUser());

                    // For collections like commuteHistory, preferredRoutes, handle carefully (e.g. replace, merge, or ignore here)

                    CommutePlan saved = commutePlanRepository.save(existingPlan);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommutePlan(@PathVariable Long id) {
        if (commutePlanRepository.existsById(id)) {
            commutePlanRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
