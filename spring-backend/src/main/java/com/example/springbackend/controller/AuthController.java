package com.example.springbackend.controller;

import com.example.springbackend.dto.*;
import com.example.springbackend.dto.request.AuthRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        return authService.login(authRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof BadCredentialsException) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .build());
                    }
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                });
    }


    @PostMapping("/register")
    public Mono<ResponseEntity<MessageResponse>> register(@RequestBody AuthRequest authRequest) {
        return authService.register(authRequest)
                .map(result -> ResponseEntity.ok(new MessageResponse((String)result)))
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity
                                .status(409) // Conflict
                                .body(new MessageResponse(e.getMessage())));
                    }
                    return Mono.just(ResponseEntity
                            .status(500)
                            .body(new MessageResponse("An unexpected error occurred")));
                });
    }

}
