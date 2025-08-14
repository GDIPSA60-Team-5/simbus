package com.example.springbackend.service;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.repository.DeviceTokenMongoRepository;
import com.example.springbackend.repository.NotificationJobMongoRepository;
import com.example.springbackend.repository.RouteMongoRepository;
import com.example.springbackend.service.implementation.BusService;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationSchedulerService {

    private final NotificationJobMongoRepository jobRepository;
    private final DeviceTokenMongoRepository deviceTokenRepository;
    private final RouteMongoRepository routeRepository;
    private final FcmService fcmService;
    private final BusService busService;
    private static final Logger log = LoggerFactory.getLogger(NotificationSchedulerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public NotificationSchedulerService(NotificationJobMongoRepository jobRepository,
                                        DeviceTokenMongoRepository deviceTokenRepository,
                                        RouteMongoRepository routeRepository,
                                        FcmService fcmService, BusService busService) {
        log.info("[SCHED] Scheduler bean created");
        this.jobRepository = jobRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.routeRepository = routeRepository;
        this.fcmService = fcmService;
        this.busService = busService;
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    public void checkJobs() {
        log.info("[SCHED] Tick at {}", Instant.now());
        LocalDateTime nowUTC = LocalDateTime.now(ZoneOffset.UTC);

        jobRepository.findAll()
                .flatMap(job -> {
                    ZoneId zoneId = ZoneId.of(job.getTimezone());
                    LocalDateTime localNow = nowUTC.atZone(ZoneOffset.UTC)
                            .withZoneSameInstant(zoneId)
                            .toLocalDateTime();

                    int todayIndex = localNow.getDayOfWeek().getValue() - 1; // Monday=0, Sunday=6
                    if (todayIndex < 0) todayIndex = 6;
                    String currentTimeStr = localNow.format(DateTimeFormatter.ofPattern("HH:mm"));

                    log.info("[SCHED] job={} tz={} localNow={} todayIndex={} scheduled={} selectedDays={} status={}",
                            job.getId(), job.getTimezone(), localNow, todayIndex,
                            job.getScheduledTime(), job.getSelectedDays(), job.getStatus());

                    if ("ONGOING".equalsIgnoreCase(job.getStatus())
                            || (Boolean.TRUE.equals(job.getSelectedDays().get(todayIndex))
                            && currentTimeStr.equals(job.getScheduledTime())
                            && "PENDING".equalsIgnoreCase(job.getStatus()))) {

                        return routeRepository.findById(job.getRouteId())
                                .flatMap(route -> fetchNextBusTimings(route.getBusStop(), route.getBusService())
                                        .collectList() // collect Flux<BusArrival> into List<BusArrival>
                                        .flatMap(busArrivalsList -> {
                                            String busJson;
                                            try {
                                                busJson = objectMapper.writeValueAsString(busArrivalsList);
                                            } catch (JsonProcessingException e) {
                                                log.error("Error serializing bus arrivals to JSON", e);
                                                busJson = "[]";
                                            }

                                            String finalBusJson = busJson;
                                            return deviceTokenRepository.findByDeviceId(job.getDeviceId())
                                                    .take(1)  // Take only the first result if multiple exist
                                                    .next()   // Convert Flux to Mono
                                                    .flatMap(deviceToken -> {
                                                        // send busJson as its own data field
                                                        Map<String, String> data = new HashMap<>();
                                                        data.put("title", job.getMessageTitle());
                                                        data.put("body", job.getMessageBody());
                                                        data.put("nextBus", finalBusJson);

                                                        fcmService.sendNotification(deviceToken.getFcmToken(), data);

                                                    job.setStatus("ONGOING");
                                                        return jobRepository.save(job);
                                                    });
                                        })
                                );
                    }
                    return Mono.empty(); // use Mono.empty() here because flatMap expects Mono
                })
                .subscribe();
    }

    private Flux<BusArrival> fetchNextBusTimings(String busStopQuery, String serviceNo) {
        return busService.searchBusStops(busStopQuery)
                .next() // take the first matching bus stop
                .flatMapMany(busService::getArrivalsForStop) // get arrivals for that stop
                .filter(arrival -> serviceNo == null || serviceNo.isBlank()
                        || arrival.serviceName().equalsIgnoreCase(serviceNo));
    }
}
