package com.example.core.service

import com.example.core.api.StartTripRequest
import com.example.core.api.TripApi
import com.example.core.api.UpdateProgressRequest
import com.example.core.model.Coordinates
import com.example.core.model.Route
import com.example.core.model.Trip
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripService @Inject constructor(
    private val tripApi: TripApi
) {
    
    suspend fun startTrip(
        username: String,
        startLocation: String,
        endLocation: String,
        startCoordinates: Coordinates?,
        endCoordinates: Coordinates?,
        route: Route
    ): Result<Trip> {
        return try {
            val request = StartTripRequest(
                username = username,
                startLocation = startLocation,
                endLocation = endLocation,
                startCoordinates = startCoordinates,
                endCoordinates = endCoordinates,
                route = route
            )
            
            val response = tripApi.startTrip(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to start trip: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeTrip(tripId: String): Result<Trip> {
        return try {
            val response = tripApi.completeTrip(tripId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to complete trip: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTripProgress(tripId: String, currentLegIndex: Int): Result<Trip> {
        return try {
            val request = UpdateProgressRequest(currentLegIndex)
            val response = tripApi.updateTripProgress(tripId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update trip progress: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveTrip(username: String): Result<Trip?> {
        return try {
            val response = tripApi.getActiveTrip(username)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else if (response.code() == 404) {
                Result.success(null) // No active trip
            } else {
                Result.failure(Exception("Failed to get active trip: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTripHistory(username: String): Result<List<Trip>> {
        return try {
            val response = tripApi.getTripHistory(username)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get trip history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}