package com.example.springbackend.service;

import com.example.springbackend.dto.StatsDTO;
import com.example.springbackend.repository.BotLogRepository;
import com.example.springbackend.repository.FeedbackRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final BotLogRepository botLogRepository;

    public Mono<StatsDTO> getStats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // User stats
        Mono<Long> userCount = userRepository.countUsers();
        Mono<Long> userCountRecently = userRepository.countUsersSince(sevenDaysAgo);

        // Feedback stats
        Mono<Long> feedbackCount = feedbackRepository.countFeedback();
        Mono<Long> feedbackCountRecently = feedbackRepository.countFeedbackSince(sevenDaysAgo);

        // Chatbot request stats
        Mono<Long> totalBotRequests = botLogRepository.count();
        Mono<Long> totalBotSuccess = botLogRepository.countSuccessfulResponses();
        Mono<Double> botSuccessRate = Mono.zip(totalBotRequests, totalBotSuccess)
                .map(tuple -> {
                    long requests = tuple.getT1();
                    long successes = tuple.getT2();
                    return requests > 0 ? (successes * 100.0 / requests) : 0.0;
                });

        // Chatbot performance metrics
        Mono<Double> avgResponseTime = botLogRepository.getAverageResponseTime().defaultIfEmpty(0.0);
        Mono<Double> maxResponseTime = botLogRepository.getMaxResponseTime().defaultIfEmpty(0.0);
        Mono<Double> minResponseTime = botLogRepository.getMinResponseTime().defaultIfEmpty(0.0);

        return Mono.zip(
                objects -> new StatsDTO(
                        (Long) objects[0],     // userCount
                        (Long) objects[1],     // userCountRecently
                        (Long) objects[2],     // feedbackCount
                        (Long) objects[3],     // feedbackCountRecently
                        (Long) objects[4],     // totalBotRequests
                        (Long) objects[5],     // totalBotSuccess
                        (Double) objects[6],   // botSuccessRate
                        (Double) objects[7],   // avgResponseTime
                        (Double) objects[8],   // maxResponseTime
                        (Double) objects[9]    // minResponseTime
                ),
                userCount,
                userCountRecently,
                feedbackCount,
                feedbackCountRecently,
                totalBotRequests,
                totalBotSuccess,
                botSuccessRate,
                avgResponseTime,
                maxResponseTime,
                minResponseTime
        );
    }
}