package com.example.springbackend.service.implementation;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.ErrorResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.service.BotLogService;
import com.example.springbackend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@ConditionalOnProperty(name = "chatbot.strategy", havingValue = "proxy")
public class ProxyChatbotService implements ChatbotService {

    private final WebClient llmClient;
    private final BotLogService botLogService;

    public ProxyChatbotService(WebClient.Builder webClientBuilder,
                               @Value("${chatbot.proxy.base-url}") String baseUrl,
                               BotLogService botLogService) {
        this.llmClient = webClientBuilder.baseUrl(baseUrl).build();
        this.botLogService = botLogService;
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        Instant requestTime = Instant.now();

        return getCurrentUserId()
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
                                                    return botLogService.updateResponse(
                                                                    log, Instant.now(), response.getType(), success)
                                                            .thenReturn(response);
                                                })
                                                .onErrorResume(e ->
                                                        botLogService.updateResponse(
                                                                        log, Instant.now(), "error", false)
                                                                .thenReturn(new ErrorResponseDTO("LLM service error: " + e.getMessage()))
                                                )
                                )
                );
    }

    private Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> ((UserDetails) principal).getUsername()) // here username should be userId or map it accordingly
                .switchIfEmpty(Mono.just("0L"));
    }
}
