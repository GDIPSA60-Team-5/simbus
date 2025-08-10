package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RouteTest {

    @Test
    public void testRouteBuilderAndGetters() {
        String id = "route123";
        String routeName = "Home to Office";
        Integer estimatedTimeMin = 45;
        String startLocationId = "locStart";
        String endLocationId = "locEnd";

        List<String> routeSegmentIds = Arrays.asList("seg1", "seg2");
        List<String> commuteHistoryIds = List.of("hist1");
        List<String> preferredRouteIds = Arrays.asList("pref1", "pref2");

        Route route = Route.builder()
                .id(id)
                .routeName(routeName)
                .estimatedTimeMin(estimatedTimeMin)
                .startLocationId(startLocationId)
                .endLocationId(endLocationId)
                .routeSegmentIds(routeSegmentIds)
                .commuteHistoryIds(commuteHistoryIds)
                .preferredRouteIds(preferredRouteIds)
                .build();

        assertEquals(id, route.getId());
        assertEquals(routeName, route.getRouteName());
        assertEquals(estimatedTimeMin, route.getEstimatedTimeMin());
        assertEquals(startLocationId, route.getStartLocationId());
        assertEquals(endLocationId, route.getEndLocationId());
        assertEquals(routeSegmentIds, route.getRouteSegmentIds());
        assertEquals(commuteHistoryIds, route.getCommuteHistoryIds());
        assertEquals(preferredRouteIds, route.getPreferredRouteIds());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        Route route = new Route();

        route.setId("id456");
        route.setRouteName("Park to Mall");
        route.setEstimatedTimeMin(30);
        route.setStartLocationId("startLoc");
        route.setEndLocationId("endLoc");
        route.setRouteSegmentIds(List.of("segA"));
        route.setCommuteHistoryIds(List.of("histB"));
        route.setPreferredRouteIds(List.of("prefC"));

        assertEquals("id456", route.getId());
        assertEquals("Park to Mall", route.getRouteName());
        assertEquals(30, route.getEstimatedTimeMin());
        assertEquals("startLoc", route.getStartLocationId());
        assertEquals("endLoc", route.getEndLocationId());
        assertEquals(List.of("segA"), route.getRouteSegmentIds());
        assertEquals(List.of("histB"), route.getCommuteHistoryIds());
        assertEquals(List.of("prefC"), route.getPreferredRouteIds());
    }
}
