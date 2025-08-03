package com.example.springbackend.controller;

import com.example.springbackend.dto.BotResponseDTO;
import com.example.springbackend.dto.ChatRequest;
import com.example.springbackend.dto.ErrorResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ChatbotControllerV2 {

    private final WebClient llmClient;

    public ChatbotControllerV2(WebClient.Builder webClientBuilder) {
        this.llmClient = webClientBuilder
                .baseUrl("http://localhost:8000") // FastAPI host/port
                .build();
    }

    @PostMapping("/api/v2/chatbot")
    public Mono<BotResponseDTO> handleChatInput(
            @RequestBody ChatRequest request,
            @RequestHeader HttpHeaders incomingHeaders
    ) {
        return llmClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> forwardAuthorizationHeader(incomingHeaders, headers))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BotResponseDTO.class)
                .onErrorResume(e -> Mono.just(new ErrorResponseDTO("LLM service error: " + e.getMessage())));
    }

    private void forwardAuthorizationHeader(HttpHeaders from, HttpHeaders to) {
        String auth = from.getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null) {
            to.set(HttpHeaders.AUTHORIZATION, auth);
        }
    }
}
