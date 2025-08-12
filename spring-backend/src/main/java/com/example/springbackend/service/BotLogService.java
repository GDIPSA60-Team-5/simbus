package com.example.springbackend.service;

import com.example.springbackend.model.BotLog;
import com.example.springbackend.repository.BotLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BotLogService {

    private final BotLogRepository botLogRepository;

    public Mono<BotLog> logRequest(Long userId, Instant requestTime, String userInput) {
        BotLog log = BotLog.builder()
                .userId(userId)
                .requestTime(requestTime)
                .userInput(userInput)
                .success(false)
                .build();
        return botLogRepository.save(log);
    }

    public Mono<BotLog> updateResponse(BotLog log, Instant responseTime, String responseType, boolean success) {
        log.setResponseTime(responseTime);
        log.setResponseType(responseType);
        if (!responseType.equals("error")) {
            log.setSuccess(success);
        }
        return botLogRepository.save(log);
    }
}

