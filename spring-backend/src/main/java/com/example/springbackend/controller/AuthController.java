package com.example.springbackend.controller;

import com.example.springbackend.dto.MessageResponse;
import com.example.springbackend.dto.request.AuthRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ---- Login: returns AuthResponse on 200, error MessageResponse on 401/409/500 ----
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        // input validation error -> 409 with message
                        return Mono.just(ResponseEntity
                                .status(409)
                                .body(new AuthResponse(null))); // Body type must be AuthResponse here
                    }
                    if (e instanceof BadCredentialsException) {
                        // bad credentials -> 401
                        return Mono.just(ResponseEntity
                                .status(401)
                                .body(new AuthResponse(null)));
                    }
                    return Mono.just(ResponseEntity
                            .status(500)
                            .body(new AuthResponse(null)));
                });
    }

    // ---- Register: always returns MessageResponse ----
    @PostMapping("/register")
    public Mono<ResponseEntity<MessageResponse>> register(@RequestBody AuthRequest request) {
        return authService.register(request)
                .map(msg -> ResponseEntity.ok(new MessageResponse(msg))) // Mono<ResponseEntity<MessageResponse>>
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity
                                .status(409)
                                .body(new MessageResponse(e.getMessage())));
                    }
                    return Mono.just(ResponseEntity
                            .status(500)
                            .body(new MessageResponse("An unexpected error occurred")));
                });
    }

    @GetMapping("/me")
    public Mono<UserDetails> getCurrentUser(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        return userDetailsMono;
    }
}
