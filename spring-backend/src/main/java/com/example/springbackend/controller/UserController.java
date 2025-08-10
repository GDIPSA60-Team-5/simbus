package com.example.springbackend.controller;

import com.example.springbackend.dto.ChangePasswordRequest;
import com.example.springbackend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/change-password")
    public Mono<ResponseEntity<String>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            Principal principal) {

        return userRepository.findByUserName(principal.getName())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                        return Mono.just(ResponseEntity.badRequest().body("password not correct"));
                    }

                    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                    return userRepository.save(user)
                            .thenReturn(ResponseEntity.ok("password changed successfully"));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("user not found")));
    }
}

