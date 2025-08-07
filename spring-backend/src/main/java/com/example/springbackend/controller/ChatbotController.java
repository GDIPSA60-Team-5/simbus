package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.service.ChatbotService;
import com.example.springbackend.service.implementation.LocalChatbotService;
import com.example.springbackend.service.implementation.ProxyChatbotService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/api/v2/chatbot")
    public Mono<BotResponseDTO> handleChatInput(
            @RequestBody Mono<ChatRequest> requestMono,
            @RequestHeader HttpHeaders headers
    ) {
        return requestMono.flatMap(request -> chatbotService.handleChatInput(request, headers));
    }
}
