package com.example.springbackend.service;

import com.example.springbackend.repository.CommutePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CommuteScheduler {

    private final CommutePlanRepository commutePlanRepository;
    private final TripService tripService;

    @Scheduled(fixedRate = 60000) // every 1 min
    public void checkCommutes() {
        LocalTime now = LocalTime.now();
        String todayCode = getDayCode(LocalDate.now().getDayOfWeek());

        commutePlanRepository.findByCommuteRecurrenceDayIdsContaining(todayCode)
                .subscribe(plan -> {
                    try {
                        LocalTime notifyTime = LocalTime.parse(plan.getNotifyAt());
                        if (now.truncatedTo(ChronoUnit.MINUTES)
                                .equals(notifyTime.truncatedTo(ChronoUnit.MINUTES))) {
                            tripService.startTripFromPlan(plan);
                        }
                    } catch (Exception e) {
                        // Skip invalid time formats
                    }
                });
    }
    
    private String getDayCode(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "mon";
            case TUESDAY -> "tue";
            case WEDNESDAY -> "wed";
            case THURSDAY -> "thu";
            case FRIDAY -> "fri";
            case SATURDAY -> "sat";
            case SUNDAY -> "sun";
        };
    }
}
