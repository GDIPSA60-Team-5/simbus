package com.example.springbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class ReverseGeocodeServiceTest {

    @Mock
    @Qualifier("oneMapWebClient")
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    ReverseGeocodeService reverseGeocodeService;

    @BeforeEach
    void setup() {
        reverseGeocodeService = new ReverseGeocodeService(webClient);
        // Set private field oneMapToken via reflection or use package-private setter if available
        // For simplicity in test, use reflection here:
        try {
            var field = ReverseGeocodeService.class.getDeclaredField("oneMapToken");
            field.setAccessible(true);
            field.set(reverseGeocodeService, "dummy-token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn((WebClient.RequestHeadersSpec<?>) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void testGetCandidates_success() {
        String jsonResponse = """
            {
              "GeocodeInfo": [
                {
                  "BUILDINGNAME": "Orchard Towers",
                  "BLOCK": "123",
                  "ROAD": "Orchard Rd",
                  "POSTALCODE": "238823"
                }
              ]
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(reverseGeocodeService.getCandidates("24291.97788882387", "31373.0117224489"))
                .expectNextMatches(list -> list.size() == 1 &&
                        list.get(0).address().contains("Orchard Towers") &&
                        list.get(0).address().contains("Block 123") &&
                        list.get(0).address().contains("Orchard Rd") &&
                        list.get(0).address().contains("Singapore 238823"))
                .verifyComplete();

        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(any(Function.class));
        verify(requestHeadersSpec, times(1)).header(eq(HttpHeaders.AUTHORIZATION), anyString());
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @Test
    void testGetCandidates_emptyResults() {
        String jsonResponse = "{\"GeocodeInfo\": []}";

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(reverseGeocodeService.getCandidates("0", "0"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void testGetCandidates_apiError() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API down")));

        StepVerifier.create(reverseGeocodeService.getCandidates("0", "0"))
                .expectNextMatches(List::isEmpty)  // fallback returns empty list on error
                .verifyComplete();
    }
}
