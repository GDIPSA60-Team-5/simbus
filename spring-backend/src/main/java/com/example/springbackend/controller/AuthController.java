package com.example.springbackend.controller;

import com.example.springbackend.dto.*;
import com.example.springbackend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return authService.login(authRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody AuthRequest authRequest) {
        String result = authService.register(authRequest);
        if (result.equals("registration successful")) {
            return ResponseEntity.ok(new MessageResponse(result));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(result));
        }
    }

}