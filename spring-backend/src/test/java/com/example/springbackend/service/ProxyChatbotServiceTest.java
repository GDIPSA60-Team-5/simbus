package com.example.springbackend.service;

import com.example.springbackend.dto.llm.*;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import com.example.springbackend.service.implementation.ProxyChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class ProxyChatbotServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private BotLogService botLogService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    private ProxyChatbotService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock WebClient builder
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        service = new ProxyChatbotService(webClientBuilder, botLogService, jwtTokenProvider, userRepository);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(mock(WebClient.RequestHeadersSpec.class));

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void testHandleChatInput_returnsDirectionsResponse() {
        DirectionsResponseDTO.LegDTO leg = new DirectionsResponseDTO.LegDTO(
                "bus", 10, "12", "Take bus 12", "encodedPolyline"
        );
        DirectionsResponseDTO.RouteDTO route = new DirectionsResponseDTO.RouteDTO(
                15, List.of(leg), "Route summary"
        );
        DirectionsResponseDTO directionsResponse = new DirectionsResponseDTO(
                "StartLoc", "EndLoc",
                new Coordinates(1.0, 2.0),
                new Coordinates(3.0, 4.0),
                List.of(route)
        );

        when(responseSpec.bodyToMono(eq(BotResponseDTO.class)))
                .thenReturn(Mono.just((BotResponseDTO) directionsResponse));

        ChatRequest request = new ChatRequest("Where is bus 12?", new Coordinates(0, 0), System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();

        Mono<BotResponseDTO> result = service.handleChatInput(request, headers);

        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp instanceof DirectionsResponseDTO &&
                                ((DirectionsResponseDTO) resp).startLocation().equals("StartLoc") &&
                                ((DirectionsResponseDTO) resp).suggestedRoutes().size() == 1
                )
                .verifyComplete();
    }

    @Test
    void testHandleChatInput_returnsMessageResponse() {
        MessageResponseDTO messageResponse = new MessageResponseDTO("Hello, how can I help?");

        when(responseSpec.bodyToMono(eq(BotResponseDTO.class)))
                .thenReturn(Mono.just((BotResponseDTO) messageResponse));

        ChatRequest request = new ChatRequest("Hi", new Coordinates(0, 0), System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();

        Mono<BotResponseDTO> result = service.handleChatInput(request, headers);

        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp instanceof MessageResponseDTO &&
                                ((MessageResponseDTO) resp).message().equals("Hello, how can I help?")
                )
                .verifyComplete();
    }

    @Test
    void testHandleChatInput_errorReturnsErrorResponse() {
        when(responseSpec.bodyToMono(eq(BotResponseDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("Service down")));

        ChatRequest request = new ChatRequest("Anything", new Coordinates(0, 0), System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();

        Mono<BotResponseDTO> result = service.handleChatInput(request, headers);

        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp instanceof ErrorResponseDTO &&
                                ((ErrorResponseDTO) resp).message().contains("LLM service error")
                )
                .verifyComplete();
    }
}


