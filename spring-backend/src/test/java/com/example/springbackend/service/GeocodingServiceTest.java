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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    @Qualifier("oneMapWebClient")
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    @Mock
    GeocodingService geocodingService;

    @BeforeEach
    void setup() throws IllegalAccessException, NoSuchFieldException {
        geocodingService = new GeocodingService(webClient);
        Field tokenField = GeocodingService.class.getDeclaredField("oneMapToken");
        tokenField.setAccessible(true);
        tokenField.set(geocodingService, "dummy-token");

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn((WebClient.RequestHeadersSpec<?>) requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void testGetCandidates_success() {
        // Sample JSON response mimicking OneMap API
        String jsonResponse = """
            {
              "results": [
                {
                  "LATITUDE": "1.3000",
                  "LONGITUDE": "103.8000",
                  "SEARCHVAL": "Orchard Rd",
                  "POSTAL": "238823",
                  "BLK_NO": "123",
                  "ROAD_NAME": "Orchard Rd",
                  "BUILDING": "Orchard Building"
                }
              ]
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(geocodingService.getCandidates("Orchard"))
                .expectNextMatches(list -> list.size() == 1
                        && list.get(0).latitude().equals("1.3000")
                        && list.get(0).longitude().equals("103.8000")
                        && list.get(0).displayName().equals("Orchard Rd"))
                .verifyComplete();

        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(any(Function.class));
        verify(requestHeadersSpec, times(1)).header(eq(HttpHeaders.AUTHORIZATION), anyString());
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @Test
    void testGetCandidates_emptyResults() {
        String jsonResponse = "{\"results\": []}";

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(geocodingService.getCandidates("Nowhere"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void testGetCandidates_apiError() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API down")));

        StepVerifier.create(geocodingService.getCandidates("ErrorTest"))
                .expectNextMatches(List::isEmpty) // fallback returns empty list on error
                .verifyComplete();
    }

    @Test
    void testGetCandidates_invalidInput() {
        StepVerifier.create(geocodingService.getCandidates(""))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().equals("locationName must not be blank"))
                .verify();
    }
}
