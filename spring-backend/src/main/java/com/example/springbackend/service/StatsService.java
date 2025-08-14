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

        //user
        Mono<Long> userCount = userRepository.countUsers();
        Mono<Long> userCountRecently= userRepository.countUsersSince(sevenDaysAgo);

        //feedback
        Mono<Long> feedbackCount = feedbackRepository.countFeedback();
        Mono<Long> feedbackCountRecently = feedbackRepository.countFeedbackSince(sevenDaysAgo);

        //chatbot
        Mono<Long> totalBotRequests = botLogRepository.count();
        Mono<Long> totalBotSuccess = botLogRepository.countSuccessfulResponses();
        Mono<Double> botSuccessRate = Mono.zip(totalBotRequests, totalBotSuccess)
                .map(t -> t.getT1() > 0 ? (t.getT2() * 100.0 / t.getT1()) : 0.0);

        //response time metrics
        Mono<Double> avgResponseTime = botLogRepository.getAverageResponseTime().defaultIfEmpty(0.0);
        Mono<Double> maxResponseTime = botLogRepository.getMaxResponseTime().defaultIfEmpty(0.0);
        Mono<Double> minResponseTime = botLogRepository.getMinResponseTime().defaultIfEmpty(0.0);

        return Mono.zip(
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
        ).map(tuple -> new StatsDTO(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                tuple.getT5(),
                tuple.getT6(),
                tuple.getT7(),
                tuple.getT8(),
                tuple.getT9(),
                tuple.getT10()
        ));
    }
}

