package com.example.springbackend.service;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.implementation.BusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
class BusServiceTest {

    @Mock
    BusServiceProvider providerA;

    @Mock
    BusServiceProvider providerB;

    BusService busService;

    @BeforeEach
    void setUp() {
        when(providerA.getApiName()).thenReturn("ProviderA");
        when(providerB.getApiName()).thenReturn("ProviderB");

        when(providerA.getAllBusStops()).thenReturn(Flux.just(
                new BusStop("001", "Alpha Stop", 1.0, 2.0, "ProviderA"),
                new BusStop("002", "Beta Stop", 3.0, 4.0, "ProviderA")
        ));

        when(providerB.getAllBusStops()).thenReturn(Flux.just(
                new BusStop("101", "Gamma Stop", 5.0, 6.0, "ProviderB")
        ));

        busService = new BusService(List.of(providerA, providerB));
    }

    @Test
    void testSearchBusStops_byName() {
        StepVerifier.create(busService.searchBusStops("alpha"))
                .expectNextMatches(stop -> stop.name().equals("Alpha Stop"))
                .verifyComplete();
    }

    @Test
    void testSearchBusStops_blankQuery_returnsAll() {
        StepVerifier.create(busService.searchBusStops(""))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testGetArrivalsForStop_validProvider() {
        BusStop stop = new BusStop("001", "Alpha Stop", 1.0, 2.0, "ProviderA");
        when(providerA.getBusArrivals("001"))
                .thenReturn(Flux.just(
                        new BusArrival(
                                "Bus 12",
                                "OperatorX",
                                List.of(ZonedDateTime.now().plusMinutes(5), ZonedDateTime.now().plusMinutes(15))
                        )
                ));

        StepVerifier.create(busService.getArrivalsForStop(stop))
                .expectNextMatches(arrival -> arrival.serviceName().equals("Bus 12"))
                .verifyComplete();
    }

    @Test
    void testGetArrivalsForStop_invalidProvider() {
        BusStop stop = new BusStop("999", "Unknown Stop", 0.0, 0.0, "UnknownProvider");

        StepVerifier.create(busService.getArrivalsForStop(stop))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException)
                .verify();
    }
}
