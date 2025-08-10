package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
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

import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    @Qualifier("oneMapWebClient")
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    RoutingService routingService;

    @BeforeEach
    void setup() {
        routingService = new RoutingService(webClient);

        // Set private oneMapToken via reflection
        try {
            var field = RoutingService.class.getDeclaredField("oneMapToken");
            field.setAccessible(true);
            field.set(routingService, "dummy-token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("some dummy json response"));
    }

    @Test
    void testGetBusRoutes_successWithoutArrivalTimeFilter() {
        String jsonResponse = """
            {
              "plan": {
                "itineraries": [
                  {
                    "duration": 900,
                    "legs": [
                      {
                        "mode": "BUS",
                        "duration": 900,
                        "routeShortName": "123",
                        "from": {"name": "Start"},
                        "to": {"name": "End"},
                        "legGeometry": {"points": "abcd"}
                      }
                    ]
                  },
                  {
                    "duration": 1200,
                    "legs": []
                  }
                ]
              }
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(routingService.getBusRoutes("1.3521,103.8198", "1.290270,103.851959", null))
                .assertNext(routes -> {
                    assertEquals(2, routes.size());
                    DirectionsResponseDTO.RouteDTO first = routes.get(0);
                    assertEquals(15, first.durationInMinutes());
                    assertEquals("Bus Service 123", first.summary());
                    assertEquals(1, first.legs().size());
                    assertEquals("BUS", first.legs().get(0).type());
                    assertEquals("abcd", first.legs().get(0).legGeometry());
                })
                .verifyComplete();

        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(any(Function.class));
        verify(requestHeadersSpec, times(1)).header(eq(HttpHeaders.AUTHORIZATION), anyString());
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @Test
    void testGetBusRoutes_filtersByArrivalTime() {
        String jsonResponse = """
            {
              "plan": {
                "itineraries": [
                  {
                    "duration": 900,
                    "legs": [
                      {
                        "mode": "BUS",
                        "duration": 900,
                        "routeShortName": "123",
                        "from": {"name": "Start"},
                        "to": {"name": "End"},
                        "legGeometry": {"points": "abcd"}
                      }
                    ]
                  },
                  {
                    "duration": 3600,
                    "legs": []
                  }
                ]
              }
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        // Use arrivalTime that excludes the 3600 seconds route
        LocalTime arrivalTime = LocalTime.now().plusMinutes(20);

        StepVerifier.create(routingService.getBusRoutes("1.3521,103.8198", "1.290270,103.851959", arrivalTime))
                .assertNext(routes -> {
                    assertEquals(1, routes.size());  // Only first route included
                    assertEquals(15, routes.get(0).durationInMinutes());
                })
                .verifyComplete();
    }

    @Test
    void testGetBusRoutes_emptyPlan() {
        String jsonResponse = """
            {
              "plan": null
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(routingService.getBusRoutes("1.0,2.0", "3.0,4.0", null))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void testGetBusRoutes_invalidStartCoordinate() {
        StepVerifier.create(routingService.getBusRoutes("invalid", "1.0,2.0", null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Invalid start coordinate format"))
                .verify();
    }

    @Test
    void testGetBusRoutes_invalidEndCoordinate() {
        StepVerifier.create(routingService.getBusRoutes("1.0,2.0", "invalid", null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Invalid end coordinate format"))
                .verify();
    }

    @Test
    void testGetBusRoutes_apiError() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API error")));

        StepVerifier.create(routingService.getBusRoutes("1.3521,103.8198", "1.290270,103.851959", null))
                .expectError()
                .verify();
    }
}
