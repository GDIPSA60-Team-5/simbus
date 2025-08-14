package com.example.springbackend.service;

import com.example.springbackend.dto.request.AuthRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.security.UserDetailsAuthenticationManager;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsAuthenticationManager authenticationManager;

    // Allowed username characters: letters, digits, underscore, dot, hyphen
    private static final Pattern USERNAME_SAFE = Pattern.compile("^[A-Za-z0-9._-]+$");

    public AuthService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       @Qualifier("userDetailsAuthenticationManager")
                       UserDetailsAuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Login validation and authentication:
     * (1) Username and password cannot be null or blank -> "Parameters cannot be empty"
     * (2) Username may only contain safe characters -> "Username contains invalid characters"
     * (3) Username length must be at least 4 -> "Username is too short"
     * (4) Password length must be at least 8 -> "Password is too short"
     * (5) If the user does not exist or the password is incorrect -> "Login failed: username and password do not match"
     */
    public Mono<AuthResponse> login(AuthRequest authRequest) {
        String username = authRequest.username();
        String password = authRequest.password();

        // (1) Null or blank check
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Mono.error(new IllegalArgumentException("Parameters cannot be empty"));
        }
        // (3) Username length
        if (username.length() < 4) {
            return Mono.error(new IllegalArgumentException("Username is too short"));
        }
        // (2) Username character set
        if (!USERNAME_SAFE.matcher(username).matches()) {
            return Mono.error(new IllegalArgumentException("Username contains invalid characters"));
        }
        // (4) Password length
        if (password.length() < 8) {
            return Mono.error(new IllegalArgumentException("Password is too short"));
        }

        // (5) Check if the user exists; if not, return login failure immediately
        return userRepository.findByUserName(username)
                .hasElement()
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BadCredentialsException("Login failed: username and password do not match"));
                    }
                    // If exists, proceed to authentication; map any authentication failure to the same login failure message
                    UsernamePasswordAuthenticationToken token =
                            new UsernamePasswordAuthenticationToken(username, password);

                    return authenticationManager.authenticate(token)
                            .onErrorResume(ex -> Mono.error(new BadCredentialsException("Login failed: username and password do not match")))
                            .map(auth -> jwtTokenProvider.generateToken(username))
                            .map(AuthResponse::new);
                });
    }

    // Registration: perform 5 validation checks
    public Mono<String> register(AuthRequest authRequest) {
        String username = authRequest.username();
        String password = authRequest.password();

        // (1) Username and password cannot be null or blank
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Mono.error(new IllegalArgumentException("Parameters cannot be empty"));
        }

        // (3) Username must be at least 4 characters
        if (username.length() < 4) {
            return Mono.error(new IllegalArgumentException("Username is too short"));
        }

        // (2) Username may only contain safe characters; disallow unusual special characters (e.g., ɑː)
        if (!USERNAME_SAFE.matcher(username).matches()) {
            return Mono.error(new IllegalArgumentException("Username contains invalid characters"));
        }

        // (4) Password must be at least 8 characters
        if (password.length() < 8) {
            return Mono.error(new IllegalArgumentException("Password is too short"));
        }

        // (5) Username must be unique — use hasElement() to check existence, then branch accordingly
        return userRepository.findByUserName(username)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return userRepository.save(
                                    User.builder()
                                            .userName(username)
                                            .passwordHash(passwordEncoder.encode(password)) // Store encrypted password
                                            .createdAt(new Date())
                                            .userType("user")
                                            .build()
                            )
                            .thenReturn("registration successful");
                });
    }
}