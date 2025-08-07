package com.example.springbackend.repository;

import com.example.springbackend.model.Location;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LocationRepository extends ReactiveCrudRepository<Location, String> {
}
