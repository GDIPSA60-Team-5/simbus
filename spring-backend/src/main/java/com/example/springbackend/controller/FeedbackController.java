package com.example.springbackend.controller;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public Flux<Feedback> getAllFeedbacks() {
        return feedbackService.findAll();
    }

    @GetMapping("/id/{id}")
    public Mono<ResponseEntity<Feedback>> getFeedbackById(@PathVariable String id) {
        return feedbackService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public Flux<Feedback> getFeedbackByUsername(@PathVariable String username) {
        return feedbackService.findByUserName(username);
    }

    @PostMapping
    public Mono<Feedback> createFeedback(@RequestBody Feedback feedback) {
        return feedbackService.save(feedback);
    }

    @PutMapping("/id/{id}")
    public Mono<ResponseEntity<Feedback>> updateFeedback(@PathVariable String id, @RequestBody Feedback feedback) {
        return feedbackService.update(id, feedback)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/id/{id}")
    public Mono<ResponseEntity<Void>> deleteFeedback(@PathVariable String id) {
        return feedbackService.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
