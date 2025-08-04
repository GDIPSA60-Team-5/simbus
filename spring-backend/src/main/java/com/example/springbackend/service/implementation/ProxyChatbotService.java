package com.example.springbackend.service.implementation;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.ErrorResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.service.ChatbotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;

@Service
@ConditionalOnProperty(name = "chatbot.strategy", havingValue = "proxy")
public class ProxyChatbotService implements ChatbotService {
    private final WebClient llmClient;

    public ProxyChatbotService(WebClient.Builder webClientBuilder) {
        this.llmClient = webClientBuilder
                .baseUrl("http://localhost:8000")
                .build();
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        return llmClient.post()
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
                .onErrorResume(e -> Mono.just(new ErrorResponseDTO("LLM service error: " + e.getMessage())));
    }

}
