package com.example.springbackend.service;

import com.example.springbackend.dto.MessageResponse;
import com.example.springbackend.dto.request.LoginRequest;
import com.example.springbackend.dto.request.RegisterRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.security.UserDetailsAuthenticationManager;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class AuthService {

    private final UserDetailsAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(@Qualifier("userDetailsAuthenticationManager") UserDetailsAuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<AuthResponse> login(LoginRequest authRequest) {
        return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()))
                .map(auth -> {
                    String token = jwtTokenProvider.generateToken(authRequest.username());
                    return new AuthResponse(token);
                });
    }
    public Mono<MessageResponse> register(RegisterRequest request) {
        return userRepository.findByUserName(request.username())
                .cast(MessageResponse.class)
                .flatMap(existingUser ->
                        Mono.<MessageResponse>error(new IllegalArgumentException("Username already in use"))
                )
                .switchIfEmpty(
                        Mono.defer(() ->
                                userRepository.save(
                                        User.builder()
                                                .userName(request.username())
                                                .email(request.email())
                                                .passwordHash(passwordEncoder.encode(request.password()))
                                                .createdAt(new Date())
                                                .build()
                                ).then(Mono.just(new MessageResponse("Registration successful")))
                        )
                );
    }
}
