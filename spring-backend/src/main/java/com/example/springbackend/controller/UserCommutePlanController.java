package com.example.springbackend.controller;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.model.CommuteRecurrenceDay;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.CommutePlanRepository;
import com.example.springbackend.repository.CommuteRecurrenceDayRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/user/commute-plans")
public class UserCommutePlanController {

    private final CommutePlanRepository commutePlanRepository;
    private final UserRepository userRepository;
    private final CommuteRecurrenceDayRepository recurrenceRepo;

    public UserCommutePlanController(
            CommutePlanRepository commutePlanRepository,
            UserRepository userRepository,
            CommuteRecurrenceDayRepository recurrenceRepo
    ) {
        this.commutePlanRepository = commutePlanRepository;
        this.userRepository = userRepository;
        this.recurrenceRepo = recurrenceRepo;
    }

    public record CommutePlanDto(
            String id,
            String userId,
            String commutePlanName,
            String notifyAt,  // Changed to String to match Android expectation
            String arrivalTime,  // Changed to String to match Android expectation
            Integer notificationNum,
            Boolean recurrence,
            String busStopCode,
            String busServiceNo,
            String startLocationId,
            String endLocationId,
            List<Boolean> selectedDays,
            String createdAt,
            String updatedAt
    ) {}

    // Create - Return DTO instead of CommutePlan
    @PostMapping
    public Mono<ResponseEntity<CommutePlanDto>> createCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @Valid @RequestBody CommutePlanDto dto
    ) {
        return getUserFromPrincipal(principal)
                .flatMap(user -> {
                    CommutePlan plan = CommutePlan.builder()
                            .id(dto.id())
                            .userId(user.getId())
                            .commutePlanName(dto.commutePlanName())
                            .notifyAt(parseTime(dto.notifyAt()))
                            .arrivalTime(parseTime(dto.arrivalTime()))
                            .notificationNum(dto.notificationNum())
                            .recurrence(dto.recurrence())
                            .busStopCode(dto.busStopCode())
                            .busServiceNo(dto.busServiceNo())
                            .startLocationId(dto.startLocationId())
                            .endLocationId(dto.endLocationId())
                            .build();
                    return commutePlanRepository.save(plan)
                            .flatMap(saved -> syncSelectedDays(saved.getId(), dto.selectedDays())
                                    .then(toDto(saved)));
                })
                .map(responseDto -> ResponseEntity.status(HttpStatus.CREATED).body(responseDto))
                .onErrorResume(error -> {
                    log.error("Error creating commute plan", error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // Get all (DTO)
    @GetMapping("/me")
    public Flux<CommutePlanDto> getMyCommutes(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return getUserFromPrincipal(principal)
                .flatMapMany(user -> commutePlanRepository.findByUserId(user.getId()))
                .flatMap(this::toDto)
                .sort((a, b) -> {
                    // Sort by name, then by id for consistent ordering
                    int nameCompare = (a.commutePlanName() != null ? a.commutePlanName() : "")
                            .compareTo(b.commutePlanName() != null ? b.commutePlanName() : "");
                    return nameCompare != 0 ? nameCompare :
                            (a.id() != null ? a.id() : "").compareTo(b.id() != null ? b.id() : "");
                })
                .onErrorResume(error -> {
                    log.error("Error fetching user commute plans", error);
                    return Flux.empty();
                });
    }

    // Get one (DTO)
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommutePlanDto>> getCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id) {
        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId()))
                .flatMap(this::toDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("Error fetching commute plan with id: {}", id, error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommutePlanDto>> updateCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id,
            @Valid @RequestBody CommutePlanDto dto
    ) {
        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId()))
                .flatMap(existing -> {
                    CommutePlan updated = applyUpdates(existing, dto);
                    // First save the commute plan, then sync selected days, then return DTO
                    return commutePlanRepository.save(updated)
                            .flatMap(saved -> {
                                return syncSelectedDays(saved.getId(), dto.selectedDays())
                                        .then(toDto(saved))
                                        .onErrorResume(dayError -> {
                                            log.error("Error syncing selected days for plan {}", saved.getId(), dayError);
                                            return toDto(saved);
                                        });
                            });
                })
                .map(responseDto -> ResponseEntity.ok(responseDto))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("Error updating commute plan with id: {}", id, error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // Delete
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCommutePlan(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id
    ) {
        return getUserFromPrincipal(principal)
                .flatMap(user -> findCommutePlanByIdAndUserId(id, user.getId()))
                .flatMap(plan ->
                        recurrenceRepo.deleteByCommutePlanId(plan.getId())
                                .then(commutePlanRepository.delete(plan))
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("Error deleting commute plan with id: {}", id, error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    private Mono<User> getUserFromPrincipal(Mono<UserDetails> principal) {
        return principal
                .flatMap(ud -> userRepository.findByUserName(ud.getUsername()))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    private Mono<CommutePlan> findCommutePlanByIdAndUserId(String id, String userId) {
        return commutePlanRepository.findById(id)
                .filter(plan -> userId.equals(plan.getUserId()))
                .switchIfEmpty(Mono.empty());
    }

    private CommutePlan applyUpdates(CommutePlan existing, CommutePlanDto u) {
        return CommutePlan.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .commutePlanName(u.commutePlanName() != null ? u.commutePlanName() : existing.getCommutePlanName())
                .notifyAt(u.notifyAt() != null ? parseTime(u.notifyAt()) : existing.getNotifyAt())
                .arrivalTime(u.arrivalTime() != null ? parseTime(u.arrivalTime()) : existing.getArrivalTime())
                .notificationNum(u.notificationNum() != null ? u.notificationNum() : existing.getNotificationNum())
                .recurrence(u.recurrence() != null ? u.recurrence() : existing.getRecurrence())
                .busStopCode(u.busStopCode() != null ? u.busStopCode() : existing.getBusStopCode())
                .busServiceNo(u.busServiceNo() != null ? u.busServiceNo() : existing.getBusServiceNo())
                .startLocationId(u.startLocationId() != null ? u.startLocationId() : existing.getStartLocationId())
                .endLocationId(u.endLocationId() != null ? u.endLocationId() : existing.getEndLocationId())
                .build();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            log.warn("Failed to parse time: {}", timeStr);
            return null;
        }
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : null;
    }

    private Mono<Void> syncSelectedDays(String commutePlanId, List<Boolean> selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            log.debug("No selectedDays provided for commutePlanId: {}, skipping sync", commutePlanId);
            return Mono.empty();
        }

        log.debug("Syncing selected days for commutePlanId: {} with days: {}", commutePlanId, selectedDays);

        return recurrenceRepo.deleteByCommutePlanId(commutePlanId)
                .doOnSuccess(v -> log.debug("Deleted existing recurrence days for plan: {}", commutePlanId))
                .thenMany(
                        Flux.fromIterable(selectedDays)
                                .index()
                                .filter(tuple -> Boolean.TRUE.equals(tuple.getT2()))
                                .map(tuple -> {
                                    int dayIndex = tuple.getT1().intValue();
                                    int dayOfWeek = (dayIndex % 7) + 1; // Ensure 1-7 range
                                    return CommuteRecurrenceDay.builder()
                                            .commutePlanId(commutePlanId)
                                            .dayOfWeek(dayOfWeek)
                                            .build();
                                })
                                .doOnNext(day -> log.debug("Creating recurrence day: {} for plan: {}",
                                        day.getDayOfWeek(), commutePlanId))
                )
                .flatMap(day -> recurrenceRepo.save(day)
                        .doOnSuccess(saved -> log.debug("Saved recurrence day: {}", saved))
                        .onErrorResume(error -> {
                            log.error("Error saving recurrence day for plan {}: {}", commutePlanId, error.getMessage());
                            return Mono.empty();
                        })
                )
                .then()
                .doOnSuccess(v -> log.debug("Successfully synced selected days for plan: {}", commutePlanId))
                .onErrorResume(error -> {
                    log.error("Error in syncSelectedDays for plan {}: {}", commutePlanId, error.getMessage(), error);
                    return Mono.error(error);
                });
    }

    private Mono<CommutePlanDto> toDto(CommutePlan plan) {
        return recurrenceRepo.findByCommutePlanId(plan.getId())
                .collectList()
                .map(days -> {
                    var list = new ArrayList<Boolean>(Collections.nCopies(7, false));
                    for (var d : days) {
                        Integer dayOfWeek = d.getDayOfWeek();
                        if (dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 7) {
                            int idx = dayOfWeek - 1; // Convert 1-7 to 0-6
                            list.set(idx, true);
                        } else {
                            log.warn("Invalid dayOfWeek value: {} for plan: {}", dayOfWeek, plan.getId());
                        }
                    }
                    return new CommutePlanDto(
                            plan.getId(),
                            plan.getUserId(),
                            plan.getCommutePlanName(),
                            formatTime(plan.getNotifyAt()),
                            formatTime(plan.getArrivalTime()),
                            plan.getNotificationNum(),
                            plan.getRecurrence(),
                            plan.getBusStopCode(),
                            plan.getBusServiceNo(),
                            plan.getStartLocationId(),
                            plan.getEndLocationId(),
                            list,
                            null, // createdAt - add if you have this field
                            null  // updatedAt - add if you have this field
                    );
                })
                .onErrorResume(error -> {
                    log.error("Error converting CommutePlan to DTO for plan: {}", plan.getId(), error);
                    return Mono.just(new CommutePlanDto(
                            plan.getId(),
                            plan.getUserId(),
                            plan.getCommutePlanName(),
                            formatTime(plan.getNotifyAt()),
                            formatTime(plan.getArrivalTime()),
                            plan.getNotificationNum(),
                            plan.getRecurrence(),
                            plan.getBusStopCode(),
                            plan.getBusServiceNo(),
                            plan.getStartLocationId(),
                            plan.getEndLocationId(),
                            new ArrayList<>(Collections.nCopies(7, false)),
                            null,
                            null
                    ));
                });
    }
}