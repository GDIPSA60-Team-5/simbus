package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RouteSegmentTest {

    @Test
    public void testRouteSegmentBuilderAndGetters() {
        String id = "seg123";
        Integer segmentOrder = 1;
        String transportMode = "Bus";
        Integer estimatedTimeMin = 15;
        String routeId = "route789";
        String fromLocationId = "locStart";
        String toLocationId = "locEnd";

        RouteSegment segment = RouteSegment.builder()
                .id(id)
                .segmentOrder(segmentOrder)
                .transportMode(transportMode)
                .estimatedTimeMin(estimatedTimeMin)
                .routeId(routeId)
                .fromLocationId(fromLocationId)
                .toLocationId(toLocationId)
                .build();

        assertEquals(id, segment.getId());
        assertEquals(segmentOrder, segment.getSegmentOrder());
        assertEquals(transportMode, segment.getTransportMode());
        assertEquals(estimatedTimeMin, segment.getEstimatedTimeMin());
        assertEquals(routeId, segment.getRouteId());
        assertEquals(fromLocationId, segment.getFromLocationId());
        assertEquals(toLocationId, segment.getToLocationId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        RouteSegment segment = new RouteSegment();

        segment.setId("seg456");
        segment.setSegmentOrder(2);
        segment.setTransportMode("Train");
        segment.setEstimatedTimeMin(20);
        segment.setRouteId("route321");
        segment.setFromLocationId("locA");
        segment.setToLocationId("locB");

        assertEquals("seg456", segment.getId());
        assertEquals(2, segment.getSegmentOrder());
        assertEquals("Train", segment.getTransportMode());
        assertEquals(20, segment.getEstimatedTimeMin());
        assertEquals("route321", segment.getRouteId());
        assertEquals("locA", segment.getFromLocationId());
        assertEquals("locB", segment.getToLocationId());
    }
}

