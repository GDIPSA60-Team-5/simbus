package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PreferredRouteTest {

    @Test
    public void testBuilderAndGetters() {
        String id = "pref123";
        String commutePlanId = "plan456";
        String routeId = "route789";

        PreferredRoute preferredRoute = PreferredRoute.builder()
                .id(id)
                .commutePlanId(commutePlanId)
                .routeId(routeId)
                .build();

        assertEquals(id, preferredRoute.getId());
        assertEquals(commutePlanId, preferredRoute.getCommutePlanId());
        assertEquals(routeId, preferredRoute.getRouteId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        PreferredRoute preferredRoute = new PreferredRoute();

        preferredRoute.setId("pref987");
        preferredRoute.setCommutePlanId("plan654");
        preferredRoute.setRouteId("route321");

        assertEquals("pref987", preferredRoute.getId());
        assertEquals("plan654", preferredRoute.getCommutePlanId());
        assertEquals("route321", preferredRoute.getRouteId());
    }
}
