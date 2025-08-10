package com.example.springbackend.repository;

import com.example.springbackend.model.Announcement;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AnnouncementRepository extends ReactiveCrudRepository<Announcement, String> {
    Flux<Announcement> findByUserId(String userID);
}
