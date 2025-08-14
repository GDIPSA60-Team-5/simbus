package com.example.springbackend.controller;

import com.example.springbackend.dto.ChangePasswordRequest;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.springbackend.model.User;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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

    @PostMapping("/fcm-token")
    public Mono<ResponseEntity<String>> updateFcmToken(
            @RequestBody UpdateFcmTokenRequest request,
            Principal principal) {
        
        return userService.updateFcmToken(principal.getName(), request.fcmToken())
                .map(user -> ResponseEntity.ok("FCM token updated successfully"))
                .onErrorReturn(ResponseEntity.badRequest().body("Failed to update FCM token"));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<User>> getCurrentUser(Principal principal) {
        return userRepository.findByUserName(principal.getName())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    public record UpdateFcmTokenRequest(String fcmToken) {}
}

