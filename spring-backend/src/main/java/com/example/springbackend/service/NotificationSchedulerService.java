package com.example.springbackend.service;

import com.example.springbackend.repository.DeviceTokenMongoRepository;
import com.example.springbackend.repository.NotificationJobMongoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.*;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationSchedulerService {

    private final NotificationJobMongoRepository jobRepository;
    private final DeviceTokenMongoRepository deviceTokenRepository;
    private final FcmService fcmService;
    private static final Logger log = LoggerFactory.getLogger(NotificationSchedulerService.class);

    public NotificationSchedulerService(NotificationJobMongoRepository jobRepository,
                                        DeviceTokenMongoRepository deviceTokenRepository,
                                        FcmService fcmService) {
        log.info("[SCHED] Scheduler bean created");
        this.jobRepository = jobRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.fcmService = fcmService;
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

                    // Monday=0, Sunday=6
                    int todayIndex = localNow.getDayOfWeek().getValue() - 1;
                    if (todayIndex < 0) todayIndex = 6;

                    String currentTimeStr = localNow.format(DateTimeFormatter.ofPattern("HH:mm"));

                    // right before your if-condition
                    log.info(
                            "[SCHED] job={} tz={} localNow={} todayIndex={} scheduled={} selectedDays={} status={}",
                            job.getId(),
                            job.getTimezone(),
                            localNow,
                            todayIndex,
                            job.getScheduledTime(),
                            job.getSelectedDays(),
                            job.getStatus()
                    );

                    if (Boolean.TRUE.equals(job.getSelectedDays().get(todayIndex))
                            && currentTimeStr.equals(job.getScheduledTime())
                            && "PENDING".equalsIgnoreCase(job.getStatus())) {

                        // Get latest fcmToken from deviceId
                        return deviceTokenRepository.findByDeviceId(job.getDeviceId())
                                .flatMap(deviceToken -> {
                                    fcmService.sendNotification(
                                            deviceToken.getFcmToken(),
                                            job.getMessageTitle(),
                                            job.getMessageBody()
                                    );
                                    job.setStatus("ONGOING");
                                    return jobRepository.save(job);
                                });
                    }

                    return Flux.empty();
                })
                .subscribe();
    }
}
