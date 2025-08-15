package com.example.springbackend.controller;

import com.example.springbackend.dto.request.LoginRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> adminLogin(@RequestBody LoginRequest authRequest) {
        return authService.adminLogin(authRequest)
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
}