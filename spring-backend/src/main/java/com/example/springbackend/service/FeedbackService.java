package com.example.springbackend.service;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Flux<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    public Mono<Feedback> findById(String id) {
        return feedbackRepository.findById(id);
    }

    public Flux<Feedback> findByUserName(String userName) {
        return feedbackRepository.findByUserName(userName);
    }

    public Mono<Feedback> save(Feedback feedback) {
        if (feedback.getSubmittedAt() == null) {
            feedback.setSubmittedAt(LocalDateTime.now());
        }
        return feedbackRepository.save(feedback);
    }

    public Mono<Feedback> update(String id, Feedback newFeedback) {
        return feedbackRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Feedback not found")))
                .flatMap(feedback -> {
                    feedback.setUserId(newFeedback.getUserId());
                    feedback.setSubmittedAt(LocalDateTime.now());
                    feedback.setUserName(newFeedback.getUserName());
                    feedback.setFeedbackText(newFeedback.getFeedbackText());
                    feedback.setRating(newFeedback.getRating());
                    feedback.setTagList(newFeedback.getTagList());
                    return feedbackRepository.save(feedback);
                });
    }

    public Mono<Void> deleteById(String id) {
        return feedbackRepository.deleteById(id);
    }
}
