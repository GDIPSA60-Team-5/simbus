package com.example.springbackend.controller;

import com.example.springbackend.dto.ResponseTypeCount;
import com.example.springbackend.repository.BotLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chatbot-analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequiredArgsConstructor
@Slf4j
public class ChatbotAnalyticsController {

    private final BotLogRepository botLogRepository;

    @GetMapping("/response-types")
    public Flux<ResponseTypeCount> getResponseTypeDistribution() {
        log.info("Fetching response type distribution");
        
        // Let's first try a simple approach - get counts for known types
        return Flux.just("directions", "next-bus", "commute-plan", "message", "error")
                .flatMap(responseType -> 
                    botLogRepository.countByResponseType(responseType)
                        .map(count -> new ResponseTypeCount(responseType, count))
                        .filter(rtc -> rtc.getCount() > 0) // Only include types with actual counts
                )
                .doOnNext(responseType -> log.info("Found response type: {} with count: {}", responseType.getId(), responseType.getCount()))
                .doOnError(error -> log.error("Error fetching response types: ", error))
                .doOnComplete(() -> log.info("Completed fetching response types"));
    }
}