package com.example.springbackend.controller;

import com.example.springbackend.dto.StatsDTO;
import com.example.springbackend.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/api/stats")
    public Mono<StatsDTO> getStats() {
        return statsService.getStats();
    }
}
