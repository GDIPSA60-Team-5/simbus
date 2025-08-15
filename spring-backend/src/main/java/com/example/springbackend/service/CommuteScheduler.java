package com.example.springbackend.service;

import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.repository.CommutePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommuteScheduler {

    private final CommutePlanRepository commutePlanRepository;
    private final TripService tripService;

    @Scheduled(fixedRate = 5000) // every 1 min
    public void checkCommutes() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        String todayCode = getDayCode(LocalDate.now().getDayOfWeek());
        log.info("Scheduler running at {} | Today code = {}", now, todayCode);

        // First, let's see ALL commute plans in the database
        commutePlanRepository.findAll()
                .collectList()
                .blockOptional()
                .ifPresentOrElse(allPlans -> {
                    log.info("Total commute plans in database: {}", allPlans.size());
                    allPlans.forEach(plan -> {
                        log.info("Plan: id={}, name={}, userId={}, recurrenceDays={}, notifyAt={}", 
                                plan.getId(), plan.getCommutePlanName(), plan.getUserId(), 
                                plan.getCommuteRecurrenceDayIds(), plan.getNotifyAt());
                    });
                }, () -> log.info("No commute plans found in database at all"));

        // Now check specific query for today
        commutePlanRepository.findByCommuteRecurrenceDayIdsContaining(todayCode)
                .collectList()
                .blockOptional()
                .ifPresentOrElse(plans -> {
                    log.info("Found {} commute plan(s) for today ({})", plans.size(), todayCode);
                    plans.forEach(plan -> {
                        try {
                            LocalTime notifyTime;
                            // Parse notifyAt string to LocalTime
                            if (plan.getNotifyAt() != null && !plan.getNotifyAt().isEmpty()) {
                                // Handle different date formats
                                notifyTime = parseNotifyAtTime(plan.getNotifyAt()).truncatedTo(ChronoUnit.MINUTES);
                            } else {
                                log.warn("Plan {} has null/empty notifyAt, skipping", plan.getId());
                                return;
                            }
                            
                            log.info("Processing plan id={} | notifyAt={} | now={}", plan.getId(), notifyTime, now);

                            if (now.equals(notifyTime)) {
                                tripService.startTripFromPlan(plan);
                                log.info("Started trip for plan id={} at {}", plan.getId(), notifyTime);
                            } else {
                                log.info("Skipping plan id={} — notify time ({}) does not match current time ({})",
                                        plan.getId(), notifyTime, now);
                            }
                        } catch (Exception e) {
                            log.error("Error processing plan id={} with notifyAt={}", plan.getId(), plan.getNotifyAt(), e);
                        }
                    });
                }, () -> log.info("No commute plans found for today ({})", todayCode));
    }

    private LocalTime parseNotifyAtTime(String notifyAtString) {
        try {
            // Try parsing as simple time format first (HH:mm)
            return LocalTime.parse(notifyAtString);
        } catch (Exception e1) {
            try {
                // Try parsing as full date-time format like "Tue Aug 12 07:30:00 SGT 2025"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(notifyAtString, formatter);
                return zonedDateTime.toLocalTime();
            } catch (Exception e2) {
                log.error("Could not parse notifyAt time: {}", notifyAtString);
                throw new IllegalArgumentException("Cannot parse notifyAt time: " + notifyAtString, e2);
            }
        }
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
