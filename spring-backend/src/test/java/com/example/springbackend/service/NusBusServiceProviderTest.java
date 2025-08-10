package com.example.springbackend.service;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.implementation.NusBusServiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NusBusServiceProviderTest {

    private WebClient webClient;
    private NusBusServiceProvider service;

    @BeforeEach
    void setup() {
        webClient = mock(WebClient.class);
        // Inject fake authKey via constructor
        service = new NusBusServiceProvider(webClient);
    }

    @Test
    void testGetAllBusStops_success() {
        String jsonResponse = """
        {
          "BusStopsResult": {
            "busstops": [
              { "caption": "01012", "name": "Stop1" },
              { "caption": "01013", "name": "Stop2" }
            ]
          }
        }
        """;

        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec headersSpec = mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/BusStops")).thenReturn(headersSpec);

        // Important fix for header method with varargs parameter:
        when(headersSpec.header(anyString(), (String[]) any())).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));


        Flux<BusStop> busStopsFlux = service.getAllBusStops();

        StepVerifier.create(busStopsFlux.log())
                .expectNextMatches(stop -> stop.code().equals("Stop1") && stop.name().equals("01012"))  // swap expected code and name
                .expectNextMatches(stop -> stop.code().equals("Stop2") && stop.name().equals("01013"))
                .verifyComplete();

        verify(webClient, times(1)).get();
    }

    @Test
    void testGetBusArrivals_success() {
        String busStopCode = "01012";

        String jsonResponse = """
            {
              "ShuttleServiceResult": {
                "shuttles": [
                  {
                    "name": "Shuttle1",
                    "_etas": [ { "eta": 5 }, { "eta": 10 } ]
                  }
                ]
              }
            }
            """;

        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec headersSpec = mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(ArgumentMatchers.any(Function.class))).thenReturn(headersSpec);

        // Mock both header(String, String) and header(String, String...) (varargs)
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), (String[]) any())).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        Flux<BusArrival> arrivalsFlux = service.getBusArrivals(busStopCode);

        StepVerifier.create(arrivalsFlux)
                .expectNextMatches(arrival -> arrival.serviceName().equals("Shuttle1"))
                .verifyComplete();

        verify(webClient, times(1)).get();
    }
}
