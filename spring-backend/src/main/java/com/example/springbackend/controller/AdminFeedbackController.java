package com.example.springbackend.controller;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.service.FeedbackService;
import com.example.springbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/feedbacks")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public AdminFeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    // Get all feedbacks (admin only)
    @GetMapping
    public Flux<Feedback> getAllFeedbacks(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByUserName(userDetails.getUsername())
                .filter(user -> "admin".equals(user.getUserType()))
                .switchIfEmpty(Mono.error(new AccessDeniedException("Admin access required")))
                .flatMapMany(user -> feedbackService.findAll());
    }

    // Get single feedback by ID (admin only)
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Feedback>> getFeedbackById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return userRepository.findByUserName(userDetails.getUsername())
                .filter(user -> "admin".equals(user.getUserType()))
                .switchIfEmpty(Mono.error(new AccessDeniedException("Admin access required")))
                .flatMap(user -> feedbackService.findById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Delete any feedback (admin only)
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFeedback(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return userRepository.findByUserName(userDetails.getUsername())
                .filter(user -> "admin".equals(user.getUserType()))
                .switchIfEmpty(Mono.error(new AccessDeniedException("Admin access required")))
                .flatMap(user -> feedbackService.findById(id))
                .flatMap(feedback -> feedbackService.deleteById(id)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}