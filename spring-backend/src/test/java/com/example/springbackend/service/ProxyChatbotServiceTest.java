package com.example.springbackend.service;

import com.example.springbackend.dto.llm.*;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.implementation.ProxyChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

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

    ProxyChatbotService service;

    @BeforeEach
    void setup() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        service = new ProxyChatbotService(webClientBuilder, "http://fake-url");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void testHandleChatInput_returnsDirectionsResponse() {
        // Prepare a DirectionsResponseDTO
        DirectionsResponseDTO.LegDTO leg = new DirectionsResponseDTO.LegDTO(
                "bus",
                15,
                "123",
                "Take bus 123",
                "encodedPolylineData",
                List.of(new Coordinates(0, 0)),
                "Start Stop",
                "End Stop",
                "08:00",
                "08:15"
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

        when(responseSpec.bodyToMono(BotResponseDTO.class)).thenReturn(Mono.just(directionsResponse));

        ChatRequest request = new ChatRequest("Where is bus 12?", new Coordinates(0,0), System.currentTimeMillis());
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

        when(responseSpec.bodyToMono(BotResponseDTO.class)).thenReturn(Mono.just(messageResponse));

        ChatRequest request = new ChatRequest("Hi", new Coordinates(0,0), System.currentTimeMillis());
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
        when(responseSpec.bodyToMono(BotResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Service down")));

        ChatRequest request = new ChatRequest("Anything", new Coordinates(0,0), System.currentTimeMillis());
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


