package com.example.springbackend.controller;

import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all users (admin only)
    @GetMapping
    public Flux<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .map(user -> {
                    // Don't expose password hash in response
                    user.setPasswordHash(null);
                    return user;
                });
    }

    // Get user by ID (admin only)
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Don't expose password hash in response
                    user.setPasswordHash(null);
                    return ResponseEntity.ok(user);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Delete user by ID (admin only)
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return userRepository.deleteById(id)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
                    } else {
                        return Mono.just(ResponseEntity.notFound().<Void>build());
                    }
                });
    }

    // Get user statistics
    @GetMapping("/stats")
    public Mono<UserStatsDTO> getUserStats() {
        return Mono.zip(
                userRepository.countUsers(),
                userRepository.count()
        ).map(tuple -> new UserStatsDTO(tuple.getT1(), tuple.getT2()));
    }

    public static class UserStatsDTO {
        private final Long totalUsers;
        private final Long allTimeUsers;

        public UserStatsDTO(Long totalUsers, Long allTimeUsers) {
            this.totalUsers = totalUsers;
            this.allTimeUsers = allTimeUsers;
        }

        public Long getTotalUsers() { return totalUsers; }
        public Long getAllTimeUsers() { return allTimeUsers; }
    }
}