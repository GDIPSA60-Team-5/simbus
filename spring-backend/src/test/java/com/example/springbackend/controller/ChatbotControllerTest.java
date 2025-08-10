package com.example.springbackend.controller;

import com.example.springbackend.config.TestSecurityConfig;
import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.MessageResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@WebFluxTest(controllers = ChatbotController.class)
@ContextConfiguration(classes = ChatbotController.class)
class ChatbotControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ChatbotService chatbotService;

    private ChatRequest sampleRequest;
    private MessageResponseDTO sampleResponse;

    @BeforeEach
    void setup() {
        sampleRequest = new ChatRequest("Hello, bot!", null, System.currentTimeMillis());

        sampleResponse = new MessageResponseDTO("Hi! How can I help you?");
    }

    @Test
    @DisplayName("POST /api/v2/chatbot returns bot response")
    void testHandleChatInput() {
        when(chatbotService.handleChatInput(
                ArgumentMatchers.any(ChatRequest.class),
                ArgumentMatchers.any(HttpHeaders.class)
        )).thenReturn(Mono.just(sampleResponse));

        webTestClient.post()
                .uri("/api/v2/chatbot")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BotResponseDTO.class)
                .consumeWith(response -> {
                    BotResponseDTO body = response.getResponseBody();
                    assert body != null;
                    assert "message".equals(body.getType());
                });
    }
}

