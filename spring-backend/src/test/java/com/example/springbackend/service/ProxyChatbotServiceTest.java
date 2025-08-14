package com.example.springbackend.service;

import com.example.springbackend.dto.llm.*;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.BotLog;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.implementation.ProxyChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class ProxyChatbotServiceTest {

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    WebClient.RequestBodySpec requestBodySpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    @Mock
    BotLogService botLogService;

    ProxyChatbotService service;


    @BeforeEach
    void setup() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        service = new ProxyChatbotService(webClientBuilder, "http://fake-llm", botLogService);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        BotLog fakeLog = new BotLog();
        fakeLog.setId("logId");

        when(botLogService.logRequest(anyString(), any(), anyString()))
                .thenReturn(Mono.just(fakeLog));

        when(botLogService.updateResponse(any(), any(), anyString(), anyBoolean()))
                .thenReturn(Mono.empty());
    }


    @Test
    void testHandleChatInput_returnsMessageResponse() {
        MessageResponseDTO messageResponse = new MessageResponseDTO("Hello, how can I help?");
        when(responseSpec.bodyToMono(BotResponseDTO.class)).thenReturn(Mono.just(messageResponse));

        ChatRequest request = new ChatRequest("Hi", new Coordinates(0, 0), System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();

        UserDetails mockUser = User.withUsername("testUser").password("pass").roles("USER").build();

        Mono<BotResponseDTO> result = service.handleChatInput(request, headers)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
                ));

        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp instanceof MessageResponseDTO &&
                                ((MessageResponseDTO) resp).message().equals("Hello, how can I help?")
                )
                .verifyComplete();
    }

    @Test
    void testHandleChatInput_errorReturnsErrorResponse() {
        when(responseSpec.bodyToMono(BotResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Service down")));

        ChatRequest request = new ChatRequest("Anything", new Coordinates(0, 0), System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();

        UserDetails mockUser = User.withUsername("testUser").password("pass").roles("USER").build();

        Mono<BotResponseDTO> result = service.handleChatInput(request, headers)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
                ));

        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp instanceof ErrorResponseDTO &&
                                ((ErrorResponseDTO) resp).message().contains("LLM service error")
                )
                .verifyComplete();
    }
}
