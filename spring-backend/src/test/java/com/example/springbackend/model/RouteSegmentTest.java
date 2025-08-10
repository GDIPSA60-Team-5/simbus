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

    @Test
    public void testEqualsHashCodeAndToString() {
        RouteSegment segment1 = RouteSegment.builder()
                .id("seg100")
                .segmentOrder(1)
                .transportMode("Bus")
                .estimatedTimeMin(10)
                .routeId("route100")
                .fromLocationId("locStart")
                .toLocationId("locEnd")
                .build();

        RouteSegment segment2 = RouteSegment.builder()
                .id("seg100")
                .segmentOrder(1)
                .transportMode("Bus")
                .estimatedTimeMin(10)
                .routeId("route100")
                .fromLocationId("locStart")
                .toLocationId("locEnd")
                .build();

        RouteSegment segment3 = RouteSegment.builder()
                .id("seg200")
                .segmentOrder(2)
                .transportMode("Train")
                .estimatedTimeMin(20)
                .routeId("route200")
                .fromLocationId("locA")
                .toLocationId("locB")
                .build();

        // Positive equals/hashCode
        assertEquals(segment1, segment2);
        assertEquals(segment1.hashCode(), segment2.hashCode());

        // Negative equals/hashCode
        assertNotEquals(segment1, segment3);
        assertNotEquals(segment1.hashCode(), segment3.hashCode());

        // equals with null and different class
        assertNotEquals(null, segment1);
        assertNotEquals("some string", segment1);

        // toString contains key field values
        String toString = segment1.toString();
        assertTrue(toString.contains("id=seg100"));
        assertTrue(toString.contains("segmentOrder=1"));
        assertTrue(toString.contains("transportMode=Bus"));
        assertTrue(toString.contains("estimatedTimeMin=10"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        RouteSegment segment = RouteSegment.builder().build();

        assertNull(segment.getId());
        assertNull(segment.getSegmentOrder());
        assertNull(segment.getTransportMode());
        assertNull(segment.getEstimatedTimeMin());
        assertNull(segment.getRouteId());
        assertNull(segment.getFromLocationId());
        assertNull(segment.getToLocationId());
    }

    @Test
    public void testEqualsWithNullFields() {
        RouteSegment seg1 = new RouteSegment();
        RouteSegment seg2 = new RouteSegment();

        // Both empty objects should be equal
        assertEquals(seg1, seg2);
        assertEquals(seg1.hashCode(), seg2.hashCode());

        // Set one field non-null in one object to test inequality
        seg2.setId("someId");
        assertNotEquals(seg1, seg2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        RouteSegment seg1 = new RouteSegment();
        seg1.setId("abc");

        RouteSegment seg2 = new RouteSegment();
        seg2.setId("abc");

        assertEquals(seg1, seg2);

        seg2.setId("def");
        assertNotEquals(seg1, seg2);
    }

    @Test
    public void testToStringHandlesNullFields() {
        RouteSegment segment = new RouteSegment();
        String str = segment.toString();

        assertNotNull(str);
        assertTrue(str.contains("RouteSegment"));
    }

    @Test
    public void testSettersAndGettersWithNulls() {
        RouteSegment segment = new RouteSegment();

        segment.setId(null);
        segment.setSegmentOrder(null);
        segment.setTransportMode(null);
        segment.setEstimatedTimeMin(null);
        segment.setRouteId(null);
        segment.setFromLocationId(null);
        segment.setToLocationId(null);

        assertNull(segment.getId());
        assertNull(segment.getSegmentOrder());
        assertNull(segment.getTransportMode());
        assertNull(segment.getEstimatedTimeMin());
        assertNull(segment.getRouteId());
        assertNull(segment.getFromLocationId());
        assertNull(segment.getToLocationId());
    }
}

