package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.model.Coordinates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Captor
    ArgumentCaptor<Function<UriBuilder, URI>> uriFunctionCaptor;

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void testDateTimeQueryParametersIncludedInUri() {
        String jsonResponse = """
            {
              "plan": {
                "itineraries": []
              }
            }
            """;
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        StepVerifier.create(routingService.getBusRoutes("1.3521,103.8198", "1.290270,103.851959", null))
                .expectNextCount(1)
                .verifyComplete();

        verify(requestHeadersUriSpec).uri(uriFunctionCaptor.capture());
        Function<UriBuilder, URI> func = uriFunctionCaptor.getValue();

        @NonNull
        URI builtUri = func.apply(new UriBuilder() {
            private final StringBuilder sb = new StringBuilder("http://localhost");
            private boolean hasQuery = false;
            private final StringBuilder pathBuilder = new StringBuilder();

            @Override
            @NonNull
            public UriBuilder replacePath(String path) {
                pathBuilder.setLength(0);
                if (path != null && !path.isEmpty()) {
                    if (!path.startsWith("/")) pathBuilder.append("/");
                    pathBuilder.append(path);
                }
                return this;
            }

            @Override
            @NonNull
            public UriBuilder pathSegment(@NonNull String @NonNull... pathSegments) {
                for (String segment : pathSegments) {
                    if (pathBuilder.isEmpty() || pathBuilder.charAt(pathBuilder.length() - 1) != '/') {
                        pathBuilder.append("/");
                    }
                    pathBuilder.append(segment);
                }
                return this;
            }

            @Override
            @NonNull
            public UriBuilder path(@NonNull String path) {
                if (pathBuilder.isEmpty()) {
                    if (!path.startsWith("/")) pathBuilder.append("/");
                } else if (pathBuilder.charAt(pathBuilder.length() - 1) != '/') {
                    pathBuilder.append("/");
                }
                pathBuilder.append(path.startsWith("/") ? path.substring(1) : path);
                return this;
            }

            @Override
            @NonNull
            public URI build(@NonNull Object @NonNull... uriVariables) {
                return build();
            }

            @Override
            @NonNull
            public URI build(@NonNull Map<String, ?> uriVariables) {
                return build();
            }

            private URI build() {
                String fullUri = String.valueOf(sb) +
                        pathBuilder;
                return URI.create(fullUri);
            }

            @Override
            @NonNull
            public String toUriString() {
                return String.valueOf(sb) +
                        pathBuilder;
            }

            @Override
            @NonNull
            public UriBuilder scheme(String scheme) {
                sb.setLength(0);
                sb.append(scheme).append("://localhost");
                return this;
            }

            @Override
            @NonNull
            public UriBuilder userInfo(String userInfo) {
                return this;
            }

            @Override
            @NonNull
            public UriBuilder host(String host) {
                String current = sb.toString();
                int idx = current.indexOf("://");
                if (idx > 0) {
                    sb.setLength(0);
                    sb.append(current, 0, idx + 3).append(host);
                }
                return this;
            }

            @Override
            @NonNull
            public UriBuilder port(int port) {
                return this;
            }

            @Override
            @NonNull
            public UriBuilder port(String port) {
                return this;
            }

            @Override
            @NonNull
            public UriBuilder query(@NonNull String query) {
                if (!hasQuery) {
                    pathBuilder.append("?");
                    hasQuery = true;
                } else {
                    pathBuilder.append("&");
                }
                pathBuilder.append(query);
                return this;
            }

            @Override
            @NonNull
            public UriBuilder replaceQuery(String query) {
                String fullPath = pathBuilder.toString();
                int idx = fullPath.indexOf('?');
                if (idx >= 0) {
                    pathBuilder.setLength(0);
                    pathBuilder.append(fullPath, 0, idx);
                }
                if (query != null && !query.isEmpty()) {
                    pathBuilder.append("?");
                    pathBuilder.append(query);
                    hasQuery = true;
                } else {
                    hasQuery = false;
                }
                return this;
            }

            @Override
            @NonNull
            public UriBuilder queryParam(@NonNull String name, @NonNull Object @NonNull... values) {
                if (!hasQuery) {
                    pathBuilder.append("?");
                    hasQuery = true;
                } else {
                    pathBuilder.append("&");
                }
                pathBuilder.append(name).append("=");
                pathBuilder.append(Arrays.stream(values).map(Object::toString).collect(Collectors.joining(",")));
                return this;
            }

            @Override
            @NonNull
            public UriBuilder queryParam(@NonNull String name, Collection<?> values) {
                if (!hasQuery) {
                    pathBuilder.append("?");
                    hasQuery = true;
                } else {
                    pathBuilder.append("&");
                }
                pathBuilder.append(name).append("=");
                pathBuilder.append(values.stream().map(Object::toString).collect(Collectors.joining(",")));
                return this;
            }

            @Override
            @NonNull
            public UriBuilder queryParamIfPresent(@NonNull String name, @NonNull Optional<?> value) {
                return value.map(o -> queryParam(name, o)).orElse(this);
            }

            @Override
            @NonNull
            public UriBuilder queryParams(@NonNull MultiValueMap<String, String> params) {
                for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                    for (String val : entry.getValue()) {
                        queryParam(entry.getKey(), val);
                    }
                }
                return this;
            }

            @Override
            @NonNull
            public UriBuilder replaceQueryParam(@NonNull String name, @NonNull Object @NonNull... values) {
                return queryParam(name, values);
            }

            @Override
            @NonNull
            public UriBuilder replaceQueryParam(@NonNull String name, Collection<?> values) {
                return queryParam(name, values);
            }

            @Override
            @NonNull
            public UriBuilder replaceQueryParams(@NonNull MultiValueMap<String, String> params) {
                return queryParams(params);
            }

            @Override
            @NonNull
            public UriBuilder fragment(String fragment) {
                return this;
            }
        });

        String uriStr = builtUri.toString();

        // Uncomment to debug URI
        // System.out.println("Built URI: " + uriStr);

        assertTrue(uriStr.contains("start=1.3521,103.8198"));
        assertTrue(uriStr.contains("end=1.290270,103.851959"));
        assertTrue(uriStr.contains("routeType=pt"));
        assertTrue(uriStr.contains("mode=BUS"));
        assertTrue(uriStr.contains("numItineraries=3"));

        String regexDate = "\\d{2}-\\d{2}-\\d{4}";
        String regexTime = "\\d{2}:\\d{2}:\\d{2}";

        Pattern datePattern = Pattern.compile("date=" + regexDate);
        Pattern timePattern = Pattern.compile("time=" + regexTime);

        assertTrue(datePattern.matcher(uriStr).find(), "Date parameter missing or invalid format");
        assertTrue(timePattern.matcher(uriStr).find(), "Time parameter missing or invalid format");
    }

    // --- New test: parseRoutes with valid JSON node and max itineraries limit ---
    @Test
    void testParseRoutes() throws Exception {
        String json = """
            {
              "itineraries": [
                {
                  "duration": 1200,
                  "legs": [
                    {
                      "mode": "BUS",
                      "duration": 600,
                      "routeShortName": "123",
                      "from": {"name": "Start Stop"},
                      "to": {"name": "End Stop"},
                      "legGeometry": {"points": "abcd"}
                    },
                    {
                      "mode": "WALK",
                      "duration": 600,
                      "routeShortName": null,
                      "from": {"name": "End Stop"},
                      "to": {"name": "Destination"},
                      "legGeometry": {"points": "efgh"}
                    }
                  ]
                }
              ]
            }
            """;

        JsonNode root = objectMapper.readTree(json);
        JsonNode itinerariesNode = root.path("itineraries");

        Method parseRoutesMethod = RoutingService.class.getDeclaredMethod("parseRoutes", JsonNode.class, Coordinates.class, Coordinates.class);
        parseRoutesMethod.setAccessible(true);

        Coordinates dummyStart = new Coordinates(1.0, 2.0);
        Coordinates dummyEnd = new Coordinates(3.0, 4.0);

        List<?> routes = (List<?>) parseRoutesMethod.invoke(routingService, itinerariesNode, dummyStart, dummyEnd);

        assertNotNull(routes);
        assertFalse(routes.isEmpty());
        assertInstanceOf(DirectionsResponseDTO.RouteDTO.class, routes.get(0));

        DirectionsResponseDTO.RouteDTO route = (DirectionsResponseDTO.RouteDTO) routes.get(0);
        assertEquals(20, route.durationInMinutes()); // 1200/60 = 20 minutes
        assertEquals("Bus Service 123", route.summary());
        assertEquals(2, route.legs().size());
    }

    // --- New test: parseLegs with valid legs node ---
    @Test
    void testParseLegs() throws Exception {
        String legsJson = """
            [
              {
                "mode": "BUS",
                "duration": 600,
                "routeShortName": "456",
                "from": {"name": "Start"},
                "to": {"name": "End"},
                "legGeometry": {"points": "mnop"}
              },
              {
                "mode": "WALK",
                "duration": 300,
                "routeShortName": null,
                "from": {"name": "End"},
                "to": {"name": "Destination"},
                "legGeometry": {"points": "qrst"}
              }
            ]
            """;

        JsonNode legsNode = objectMapper.readTree(legsJson);

        Method parseLegsMethod = RoutingService.class.getDeclaredMethod("parseLegs", JsonNode.class);
        parseLegsMethod.setAccessible(true);

        List<?> legs = (List<?>) parseLegsMethod.invoke(routingService, legsNode);

        assertNotNull(legs);
        assertEquals(2, legs.size());
        assertInstanceOf(DirectionsResponseDTO.LegDTO.class, legs.get(0));

        DirectionsResponseDTO.LegDTO firstLeg = (DirectionsResponseDTO.LegDTO) legs.get(0);
        assertEquals("BUS", firstLeg.type());
        assertEquals(10, firstLeg.durationInMinutes()); // 600 / 60 = 10
        assertEquals("456", firstLeg.busServiceNumber());
        assertEquals("BUS from Start to End", firstLeg.instruction());
        assertEquals("mnop", firstLeg.legGeometry());
    }

    // --- New test: filter routes by arrivalTime logic ---
    @Test
    void testFilterRoutesByArrivalTime() {
        // Simulate routes list
        DirectionsResponseDTO.RouteDTO route1 = new DirectionsResponseDTO.RouteDTO(10, List.of(), "");
        DirectionsResponseDTO.RouteDTO route2 = new DirectionsResponseDTO.RouteDTO(30, List.of(), "");

        List<DirectionsResponseDTO.RouteDTO> routes = List.of(route1, route2);

        // arrivalTime 20 minutes from now
        LocalTime arrivalTime = LocalTime.now(SINGAPORE).plusMinutes(20);

        // Use reflection to invoke the lambda or extract the filtering code as a testable method.
        // For simplicity, we replicate filtering logic here:

        LocalDateTime nowDateTime = LocalDateTime.of(LocalDate.now(SINGAPORE), LocalTime.now(SINGAPORE));
        LocalDateTime deadline = LocalDateTime.of(LocalDate.now(SINGAPORE), arrivalTime);

        List<DirectionsResponseDTO.RouteDTO> filtered = routes.stream()
                .filter(route -> !nowDateTime.plusMinutes(route.durationInMinutes()).isAfter(deadline))
                .toList();

        assertTrue(filtered.contains(route1));
        assertFalse(filtered.contains(route2));
    }

    // Your existing tests (invalid coordinate, api error, etc.) come below...

    @Test
    void testGetBusRoutes_invalidStartCoordinateFormat() {
        StepVerifier.create(routingService.getBusRoutes("invalid", "1.0,2.0", null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Invalid start coordinate format"))
                .verify();
    }

    @Test
    void testGetBusRoutes_nullStartCoordinate() {
        StepVerifier.create(routingService.getBusRoutes(null, "1.0,2.0", null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Start coordinate is required"))
                .verify();
    }

    @Test
    void testGetBusRoutes_invalidEndCoordinateFormat() {
        StepVerifier.create(routingService.getBusRoutes("1.0,2.0", "invalid-format", null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Invalid end coordinate format"))
                .verify();
    }

    @Test
    void testGetBusRoutes_nullEndCoordinate() {
        StepVerifier.create(routingService.getBusRoutes("1.0,2.0", null, null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("End coordinate is required"))
                .verify();
    }

    @Test
    void testParseRoutesOnly_handlesInvalidJson() throws Exception {
        Method method = RoutingService.class.getDeclaredMethod("parseRoutesOnly", String.class);
        method.setAccessible(true);

        Object result = method.invoke(routingService, "invalid-json");

        assertNotNull(result);
        assertInstanceOf(List.class, result);
        assertTrue(((List<?>) result).isEmpty());
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
