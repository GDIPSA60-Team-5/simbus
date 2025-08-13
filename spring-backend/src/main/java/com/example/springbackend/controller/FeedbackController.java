package com.example.springbackend.controller;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Get all feedbacks for the current authenticated user
    @GetMapping
    public Flux<Feedback> getUserFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return feedbackService.findByUserName(username);
    }

    // Get single feedback by ID only if it belongs to current user
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Feedback>> getUserFeedbackById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        return feedbackService.findById(id)
                .filter(feedback -> feedback.getUserName().equals(username))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Create new feedback with automatic username assignment
    @PostMapping
    public Mono<Feedback> createFeedback(
            @RequestBody Feedback feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        feedback.setUserName(userDetails.getUsername());
        return feedbackService.save(feedback);
    }

    // Update feedback only if it belongs to current user
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Feedback>> updateFeedback(
            @PathVariable String id,
            @RequestBody Feedback feedback,
            @AuthenticationPrincipal UserDetails UserDetails) {
        String username = UserDetails.getUsername();

        return feedbackService.findById(id)
                .filter(existingFeedback -> existingFeedback.getUserName().equals(username))
                .flatMap(existingFeedback -> {
                    feedback.setId(id);
                    feedback.setUserName(username); // Ensure username remains unchanged
                    return feedbackService.update(id, feedback);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Delete feedback only if it belongs to current user
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFeedback(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return feedbackService.findById(id)
                .filter(feedback -> feedback.getUserName().equals(userDetails.getUsername()))
                .flatMap(feedback -> feedbackService.deleteById(id)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())));
    }
}