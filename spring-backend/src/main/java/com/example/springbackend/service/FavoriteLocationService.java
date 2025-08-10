package com.example.springbackend.service;

import com.example.springbackend.model.FavoriteLocation;
import com.example.springbackend.repository.FavoriteLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FavoriteLocationService {

    private final FavoriteLocationRepository favoriteLocationRepository;

    // Get all favorite locations for a user
    public Flux<FavoriteLocation> getLocationsByUserId(String userId) {
        return favoriteLocationRepository.findByUserId(userId);
    }

    // Get favorite location by id
    public Mono<FavoriteLocation> getLocationById(String id) {
        return favoriteLocationRepository.findById(id);
    }

    // Create new favorite location
    public Mono<FavoriteLocation> createLocation(FavoriteLocation location) {
        return favoriteLocationRepository.save(location);
    }

    // Update existing favorite location
    public Mono<FavoriteLocation> updateLocation(String id, FavoriteLocation updatedLocation) {
        return favoriteLocationRepository.findById(id)
                .flatMap(existing -> {
                    existing.setLocationName(updatedLocation.getLocationName());
                    existing.setLatitude(updatedLocation.getLatitude());
                    existing.setLongitude(updatedLocation.getLongitude());
                    existing.setUserId(updatedLocation.getUserId());
                    return favoriteLocationRepository.save(existing);
                });
    }

    // Delete favorite location by id
    public Mono<Void> deleteLocation(String id) {
        return favoriteLocationRepository.deleteById(id);
    }
}
