package com.example.springbackend.service;

import com.example.springbackend.dto.LtaDtos;
import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.implementation.LtaBusServiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;


@SuppressWarnings({"rawtypes", "unchecked"})
public class LtaBusServiceProviderTest {

    private WebClient webClient;
    private RequestHeadersUriSpec uriSpec;
    private RequestHeadersSpec headersSpec;
    private ResponseSpec responseSpec;

    private LtaBusServiceProvider ltaService;

    private final String apiKey = "dummyApiKey";

    @BeforeEach
    public void setup() {
        webClient = mock(WebClient.class);
        uriSpec = mock(RequestHeadersUriSpec.class);
        headersSpec = mock(RequestHeadersSpec.class);
        responseSpec = mock(ResponseSpec.class);

        // Mock chain
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // Initialize service
        ltaService = new LtaBusServiceProvider(webClient);
        // Inject apiKey manually (since @Value won't work here)
        try {
            var field = LtaBusServiceProvider.class.getDeclaredField("apiKey");
            field.setAccessible(true);
            field.set(ltaService, apiKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetAllBusStops() {
        // Mock response
        LtaDtos.BusStop ltaBusStop = new LtaDtos.BusStop("1234", "Test Stop", 1.23, 4.56);
        LtaDtos.LtaBusStopsResponse response = new LtaDtos.LtaBusStopsResponse(List.of(ltaBusStop));
        when(responseSpec.bodyToMono(LtaDtos.LtaBusStopsResponse.class)).thenReturn(Mono.just(response));

        Flux<BusStop> result = ltaService.getAllBusStops();

        StepVerifier.create(result)
                .expectNextMatches(busStop ->
                        busStop.code().equals("1234")
                                && busStop.name().equals("Test Stop")
                                && busStop.latitude() == 1.23
                                && busStop.longitude() == 4.56
                                && busStop.sourceApi().equals("LTA")
                )
                .verifyComplete();

        verify(webClient).get();
        verify(uriSpec).uri(any(Function.class));
        verify(headersSpec).header("AccountKey", apiKey);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(LtaDtos.LtaBusStopsResponse.class);
    }

    @Test
    public void testGetBusArrivals() {
        String busStopCode = "1234";

        // Prepare mock response
        LtaDtos.NextBus nb1 = new LtaDtos.NextBus("2025-08-11T10:00:00+08:00[Asia/Singapore]");
        LtaDtos.NextBus nb2 = new LtaDtos.NextBus("2025-08-11T10:10:00+08:00[Asia/Singapore]");
        LtaDtos.NextBus nb3 = new LtaDtos.NextBus(null); // null arrival
        LtaDtos.Service service = new LtaDtos.Service("12", "OperatorX", nb1, nb2, nb3);
        LtaDtos.LtaArrivalsResponse response = new LtaDtos.LtaArrivalsResponse(List.of(service));

        when(responseSpec.bodyToMono(LtaDtos.LtaArrivalsResponse.class)).thenReturn(Mono.just(response));

        Flux<BusArrival> result = ltaService.getBusArrivals(busStopCode);

        StepVerifier.create(result)
                .expectNextMatches(busArrival -> {
                    if (!busArrival.serviceName().equals("12")) return false;
                    if (!busArrival.operator().equals("OperatorX")) return false;
                    if (busArrival.arrivals().size() != 2) return false;
                    ZonedDateTime expected1 = ZonedDateTime.parse("2025-08-11T10:00:00+08:00[Asia/Singapore]");
                    ZonedDateTime expected2 = ZonedDateTime.parse("2025-08-11T10:10:00+08:00[Asia/Singapore]");
                    return busArrival.arrivals().get(0).equals(expected1)
                            && busArrival.arrivals().get(1).equals(expected2);
                })
                .verifyComplete();

        verify(webClient).get();
        verify(uriSpec).uri(any(Function.class));
        verify(headersSpec).header("AccountKey", apiKey);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(LtaDtos.LtaArrivalsResponse.class);
    }
}
