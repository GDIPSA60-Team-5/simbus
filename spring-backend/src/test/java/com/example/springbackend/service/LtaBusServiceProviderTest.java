package com.example.springbackend.service;

import com.example.springbackend.dto.LtaDtos;
import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.implementation.LtaBusServiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LtaBusServiceProviderTest {

    private WebClient webClient;
    private RequestHeadersUriSpec uriSpec;
    private RequestHeadersUriSpec uriSpec2;
    private RequestHeadersSpec headersSpec;
    private ResponseSpec responseSpec;

    private LtaBusServiceProvider ltaService;

    @Value("${api.lta.key:dummyApiKey}")
    private String apiKey = "dummyApiKey"; // fallback if no property injection in test

    @BeforeEach
    public void setup() throws Exception {
        webClient = mock(WebClient.class);

        uriSpec = mock(RequestHeadersUriSpec.class);
        uriSpec2 = mock(RequestHeadersUriSpec.class);
        headersSpec = mock(RequestHeadersSpec.class);
        responseSpec = mock(ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);

        // Mock the uri(...) calls to return a new RequestHeadersUriSpec mock
        when(uriSpec.uri(anyString())).thenReturn(uriSpec2);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec2);

        // header(...) called on the uriSpec2 mock
        when(uriSpec2.header(anyString(), anyString())).thenReturn(headersSpec);

        // retrieve() returns responseSpec
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // Initialize service with mocked WebClient
        ltaService = new LtaBusServiceProvider(webClient);

        // Inject the apiKey value into the private field via reflection
        Field apiKeyField = LtaBusServiceProvider.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(ltaService, apiKey);
    }

    @Test
    public void testGetAllBusStops() {
        // Prepare mock response DTO
        LtaDtos.BusStop ltaBusStop = new LtaDtos.BusStop("1234", "Test Stop", 1.23, 4.56);
        LtaDtos.LtaBusStopsResponse response = new LtaDtos.LtaBusStopsResponse(List.of(ltaBusStop));

        when(responseSpec.bodyToMono(LtaDtos.LtaBusStopsResponse.class)).thenReturn(Mono.just(response));

        Flux<BusStop> result = ltaService.getAllBusStops();

        StepVerifier.create(result)
                .expectNextMatches(busStop ->
                        busStop.code().equals("1234") &&
                                busStop.name().equals("Test Stop") &&
                                busStop.latitude() == 1.23 &&
                                busStop.longitude() == 4.56 &&
                                busStop.sourceApi().equals("LTA")
                )
                .verifyComplete();

        verify(webClient).get();
        verify(uriSpec).uri("/BusStops");
        verify(uriSpec2).header("AccountKey", apiKey);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(LtaDtos.LtaBusStopsResponse.class);
    }

    @Test
    public void testGetBusArrivals() {
        String busStopCode = "1234";

        // Prepare mock NextBus instances with ISO 8601 datetime strings
        LtaDtos.NextBus nb1 = new LtaDtos.NextBus("2025-08-11T10:00:00+08:00[Asia/Singapore]");
        LtaDtos.NextBus nb2 = new LtaDtos.NextBus("2025-08-11T10:10:00+08:00[Asia/Singapore]");
        LtaDtos.NextBus nb3 = new LtaDtos.NextBus(null); // null arrival

        LtaDtos.Service service = new LtaDtos.Service("12", "OperatorX", nb1, nb2, nb3);
        LtaDtos.LtaArrivalsResponse response = new LtaDtos.LtaArrivalsResponse(List.of(service));

        when(responseSpec.bodyToMono(LtaDtos.LtaArrivalsResponse.class)).thenReturn(Mono.just(response));

        // uri() called with Function<UriBuilder, URI> returns uriSpec2
        when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any()))
                .thenReturn(uriSpec2);

        Flux<BusArrival> result = ltaService.getBusArrivals(busStopCode);

        StepVerifier.create(result)
                .expectNextMatches(busArrival -> {
                    if (!busArrival.serviceName().equals("12")) return false;
                    if (!busArrival.operator().equals("OperatorX")) return false;
                    if (busArrival.arrivals().size() != 2) return false;
                    ZonedDateTime expected1 = ZonedDateTime.parse("2025-08-11T10:00:00+08:00[Asia/Singapore]");
                    ZonedDateTime expected2 = ZonedDateTime.parse("2025-08-11T10:10:00+08:00[Asia/Singapore]");
                    return busArrival.arrivals().get(0).equals(expected1) && busArrival.arrivals().get(1).equals(expected2);
                })
                .verifyComplete();

        verify(webClient).get();
        verify(uriSpec).uri(any(Function.class));
        verify(uriSpec2).header("AccountKey", apiKey);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(LtaDtos.LtaArrivalsResponse.class);
    }
}
