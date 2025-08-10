package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
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

    @Test
    public void testEqualsHashCodeAndToString() {
        Route route1 = Route.builder()
                .id("id1")
                .routeName("Route 1")
                .estimatedTimeMin(20)
                .startLocationId("start1")
                .endLocationId("end1")
                .routeSegmentIds(List.of("seg1"))
                .commuteHistoryIds(List.of("hist1"))
                .preferredRouteIds(List.of("pref1"))
                .build();

        Route route2 = Route.builder()
                .id("id1")  // same id and fields to test equality
                .routeName("Route 1")
                .estimatedTimeMin(20)
                .startLocationId("start1")
                .endLocationId("end1")
                .routeSegmentIds(List.of("seg1"))
                .commuteHistoryIds(List.of("hist1"))
                .preferredRouteIds(List.of("pref1"))
                .build();

        Route route3 = Route.builder()
                .id("id2") // different id to test inequality
                .routeName("Route 2")
                .estimatedTimeMin(30)
                .startLocationId("start2")
                .endLocationId("end2")
                .routeSegmentIds(List.of("seg2"))
                .commuteHistoryIds(List.of("hist2"))
                .preferredRouteIds(List.of("pref2"))
                .build();

        // equals and hashCode positive test
        assertEquals(route1, route2);
        assertEquals(route1.hashCode(), route2.hashCode());

        // equals negative test
        assertNotEquals(route1, route3);
        assertNotEquals(route1.hashCode(), route3.hashCode());

        // equals null and different class tests
        assertNotEquals(null, route1);
        assertNotEquals("some string", route1);

        // toString test: just check contains key data (simpler than exact string match)
        String toString = route1.toString();
        assertTrue(toString.contains("id=id1"));
        assertTrue(toString.contains("routeName=Route 1"));
        assertTrue(toString.contains("estimatedTimeMin=20"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        Route route = Route.builder().build();

        assertNull(route.getId());
        assertNull(route.getRouteName());
        assertNull(route.getEstimatedTimeMin());
        assertNull(route.getStartLocationId());
        assertNull(route.getEndLocationId());
        assertNull(route.getRouteSegmentIds());
        assertNull(route.getCommuteHistoryIds());
        assertNull(route.getPreferredRouteIds());
    }

    @Test
    public void testEqualsWithNullFields() {
        Route route1 = new Route();
        Route route2 = new Route();

        // Both empty routes should be equal
        assertEquals(route1, route2);
        assertEquals(route1.hashCode(), route2.hashCode());

        // Set empty list vs null list — should not be equal
        route2.setRouteSegmentIds(Collections.emptyList());
        assertNotEquals(route1, route2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        Route route1 = new Route();
        route1.setId("abc");

        Route route2 = new Route();
        route2.setId("abc");

        assertEquals(route1, route2);

        route2.setId("def");
        assertNotEquals(route1, route2);
    }

    @Test
    public void testToStringHandlesNullFields() {
        Route route = new Route();
        String str = route.toString();

        assertNotNull(str);
        assertTrue(str.contains("Route"));
    }

    @Test
    public void testListFieldsWithEmptyAndNull() {
        Route route = new Route();

        route.setRouteSegmentIds(Collections.emptyList());
        route.setCommuteHistoryIds(null);
        route.setPreferredRouteIds(List.of());

        assertTrue(route.getRouteSegmentIds().isEmpty());
        assertNull(route.getCommuteHistoryIds());
        assertTrue(route.getPreferredRouteIds().isEmpty());
    }
}
