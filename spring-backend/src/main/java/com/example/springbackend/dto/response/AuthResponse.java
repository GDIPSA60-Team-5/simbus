package com.example.springbackend.dto.response;

public class AuthResponse {
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(String token) { this.token = token; }

    public String getToken() {
        return token;
    }
}
