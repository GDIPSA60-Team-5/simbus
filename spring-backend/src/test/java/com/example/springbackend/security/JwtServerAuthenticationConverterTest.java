package com.example.springbackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtServerAuthenticationConverterTest {

    private final JwtServerAuthenticationConverter converter = new JwtServerAuthenticationConverter();

    @Test
    void convert_WithValidBearerToken_ReturnsAuthenticationMono() {
        String token = "valid.jwt.token";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .assertNext(auth -> {
                    // The UsernamePasswordAuthenticationToken uses token as principal and credentials
                    assertEquals(token, auth.getPrincipal());
                    assertEquals(token, auth.getCredentials());
                    assertEquals("class org.springframework.security.authentication.UsernamePasswordAuthenticationToken", auth.getClass().toString());
                })
                .verifyComplete();
    }

    @Test
    void convert_WithNoAuthorizationHeader_ReturnsEmptyMono() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    void convert_WithNonBearerAuthorizationHeader_ReturnsEmptyMono() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .header(HttpHeaders.AUTHORIZATION, "Basic abcdefg")
                        .build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }
}
