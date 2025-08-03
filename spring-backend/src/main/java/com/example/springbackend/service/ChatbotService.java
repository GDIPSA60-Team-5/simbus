package com.example.springbackend.service;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;


public interface ChatbotService {
    Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders);
}
