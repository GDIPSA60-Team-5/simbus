package com.example.springbackend.controller;

import com.example.springbackend.dto.request.AuthRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.SpringBackendApplication;
import com.example.springbackend.dto.MessageResponse;
import com.example.springbackend.service.AuthService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(AuthController.class)
@ContextConfiguration(classes = AuthController.class)
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = { ReactiveSecurityAutoConfiguration.class })
public class AuthControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockitoBean
        private AuthService authService;

        // -------- LOGIN --------

        @Test
        void testLoginSuccess() {
                AuthRequest request = new AuthRequest("user", "pass");
                AuthResponse authResponse = new AuthResponse("dummyToken");

                BDDMockito.given(authService.login(request))
                                .willReturn(Mono.just(authResponse));

                webTestClient.post()
                                .uri("/api/auth/login")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(AuthResponse.class)
                                .consumeWith(response -> {
                                        AuthResponse body = response.getResponseBody();
                                        Assertions.assertNotNull(body);
                                        assertEquals(authResponse.getToken(), body.getToken());
                                });
        }

        @Test
        void testLoginBadCredentials() {
                AuthRequest request = new AuthRequest("user", "wrongpass");

                BDDMockito.given(authService.login(request))
                                .willReturn(Mono.error(new BadCredentialsException("Bad credentials")));

                webTestClient.post()
                                .uri("/api/auth/login")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody().isEmpty();
        }

        @Test
        void testLoginInternalServerError() {
                AuthRequest request = new AuthRequest("user", "pass");

                BDDMockito.given(authService.login(request))
                                .willReturn(Mono.error(new RuntimeException("Unknown error")));

                webTestClient.post()
                                .uri("/api/auth/login")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().is5xxServerError()
                                .expectBody().isEmpty();
        }

        // -------- REGISTER --------

        @Test
        void testRegisterSuccess() {
                AuthRequest request = new AuthRequest("newuser", "newpass");
                String successMessage = "User registered successfully";

                BDDMockito.given(authService.register(request))
                                .willReturn(Mono.just(successMessage));

                webTestClient.post()
                                .uri("/api/auth/register")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(MessageResponse.class)
                                .consumeWith(response -> {
                                        MessageResponse body = response.getResponseBody();
                                        Assertions.assertNotNull(body);
                                        assertEquals(successMessage, body.getMessage());
                                });
        }

        @Test
        void testRegisterConflict() {
                AuthRequest request = new AuthRequest("existinguser", "pass");
                String errorMessage = "Username already exists";

                BDDMockito.given(authService.register(request))
                                .willReturn(Mono.error(new IllegalArgumentException(errorMessage)));

                webTestClient.post()
                                .uri("/api/auth/register")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                                .expectBody(MessageResponse.class)
                                .consumeWith(response -> {
                                        MessageResponse body = response.getResponseBody();
                                        Assertions.assertNotNull(body);
                                        assertEquals(errorMessage, body.getMessage());
                                });
        }

        @Test
        void testRegisterInternalServerError() {
                AuthRequest request = new AuthRequest("user", "pass");

                BDDMockito.given(authService.register(request))
                                .willReturn(Mono.error(new RuntimeException("Unknown error")));

                webTestClient.post()
                                .uri("/api/auth/register")
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().is5xxServerError()
                                .expectBody(MessageResponse.class)
                                .consumeWith(response -> {
                                        MessageResponse body = response.getResponseBody();
                                        Assertions.assertNotNull(body);
                                        assertEquals("An unexpected error occurred", body.getMessage());
                                });
        }
}
