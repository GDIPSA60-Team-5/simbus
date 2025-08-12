package com.example.springbackend.service.implementation;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.ErrorResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import com.example.springbackend.service.BotLogService;
import com.example.springbackend.service.ChatbotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;

import java.time.Instant;

@Service
@ConditionalOnProperty(name = "chatbot.strategy", havingValue = "proxy")
public class ProxyChatbotService implements ChatbotService {
    private final WebClient llmClient;
    private final BotLogService botLogService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public ProxyChatbotService(WebClient.Builder webClientBuilder, BotLogService botLogService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.llmClient = webClientBuilder
                .baseUrl("http://localhost:8000")
                .build();
        this.botLogService = botLogService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        Instant requestTime = Instant.now();

        return extractUserId(incomingHeaders)
                .flatMap(userId ->
                        botLogService.logRequest(userId, requestTime, request.userInput())
                                .flatMap(log ->
                                        llmClient.post()
                                                .uri("/chat")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .headers(headers -> {
                                                    String auth = incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION);
                                                    if (auth != null) {
                                                        headers.set(HttpHeaders.AUTHORIZATION, auth);
                                                    }
                                                })
                                                .bodyValue(request)
                                                .retrieve()
                                                .bodyToMono(BotResponseDTO.class)
                                                .flatMap(response -> {
                                                    boolean success = !(response instanceof ErrorResponseDTO);
                                                    return botLogService.updateResponse(log, Instant.now(), response.getType(), success)
                                                            .thenReturn(response);
                                                })
                                                .onErrorResume(e ->
                                                        botLogService.updateResponse(log, Instant.now(), "error", false)
                                                                .thenReturn(new ErrorResponseDTO("LLM service error: " + e.getMessage()))
                                                )
                                )
                );
    }

    private Mono<Long> extractUserId(HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(0L);
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return Mono.just(0L);
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        if (username == null || username.isBlank()) {
            return Mono.just(0L);
        }
        return userRepository.findByUserName(username)
                .map(User::getId)
                .defaultIfEmpty(0L);
    }
}


