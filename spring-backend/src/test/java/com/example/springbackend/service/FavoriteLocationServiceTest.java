package com.example.springbackend.service;

import com.example.springbackend.model.FavoriteLocation;
import com.example.springbackend.repository.FavoriteLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FavoriteLocationServiceTest {

    @Mock
    FavoriteLocationRepository favoriteLocationRepository;

    FavoriteLocationService favoriteLocationService;

    @BeforeEach
    void setup() {
        favoriteLocationService = new FavoriteLocationService(favoriteLocationRepository);
    }

    @Test
    void testGetLocationsByUserId() {
        String userId = "user1";
        FavoriteLocation loc1 = new FavoriteLocation("1", "Home", 1.23, 4.56, userId);
        FavoriteLocation loc2 = new FavoriteLocation("2", "Work", 7.89, 0.12, userId);

        when(favoriteLocationRepository.findByUserId(userId)).thenReturn(Flux.just(loc1, loc2));

        StepVerifier.create(favoriteLocationService.getLocationsByUserId(userId))
                .expectNext(loc1)
                .expectNext(loc2)
                .verifyComplete();

        verify(favoriteLocationRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetLocationById() {
        FavoriteLocation loc = new FavoriteLocation("1", "Home", 1.23, 4.56, "user1");

        when(favoriteLocationRepository.findById("1")).thenReturn(Mono.just(loc));

        StepVerifier.create(favoriteLocationService.getLocationById("1"))
                .expectNext(loc)
                .verifyComplete();

        verify(favoriteLocationRepository, times(1)).findById("1");
    }

    @Test
    void testCreateLocation() {
        FavoriteLocation loc = new FavoriteLocation(null, "Park", 12.34, 56.78, "user1");
        FavoriteLocation savedLoc = new FavoriteLocation("100", "Park", 12.34, 56.78, "user1");

        when(favoriteLocationRepository.save(loc)).thenReturn(Mono.just(savedLoc));

        StepVerifier.create(favoriteLocationService.createLocation(loc))
                .expectNext(savedLoc)
                .verifyComplete();

        verify(favoriteLocationRepository, times(1)).save(loc);
    }

    @Test
    void testUpdateLocation_existingLocation() {
        FavoriteLocation existing = new FavoriteLocation("1", "Old Name", 0.0, 0.0, "user1");
        FavoriteLocation updated = new FavoriteLocation(null, "New Name", 9.99, 8.88, "user1");
        FavoriteLocation saved = new FavoriteLocation("1", "New Name", 9.99, 8.88, "user1");

        when(favoriteLocationRepository.findById("1")).thenReturn(Mono.just(existing));
        when(favoriteLocationRepository.save(any(FavoriteLocation.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(favoriteLocationService.updateLocation("1", updated))
                .expectNext(saved)
                .verifyComplete();

        verify(favoriteLocationRepository, times(1)).findById("1");
        verify(favoriteLocationRepository, times(1)).save(any(FavoriteLocation.class));
    }

    @Test
    void testUpdateLocation_notFound() {
        FavoriteLocation updated = new FavoriteLocation(null, "New Name", 9.99, 8.88, "user1");

        when(favoriteLocationRepository.findById("1")).thenReturn(Mono.empty());

        StepVerifier.create(favoriteLocationService.updateLocation("1", updated))
                .expectComplete() // completes empty because flatMap never triggered
                .verify();

        verify(favoriteLocationRepository, times(1)).findById("1");
        verify(favoriteLocationRepository, never()).save(any());
    }

    @Test
    void testDeleteLocation() {
        when(favoriteLocationRepository.deleteById("1")).thenReturn(Mono.empty());

        StepVerifier.create(favoriteLocationService.deleteLocation("1"))
                .verifyComplete();

        verify(favoriteLocationRepository, times(1)).deleteById("1");
    }
}
