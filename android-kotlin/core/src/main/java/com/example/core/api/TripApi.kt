package com.example.core.api

import com.example.core.model.Coordinates
import com.example.core.model.Route
import com.example.core.model.Trip
import retrofit2.Response
import retrofit2.http.*

interface TripApi {
    
    @POST("/api/trips/start")
    suspend fun startTrip(@Body request: StartTripRequest): Response<Trip>
    
    @PUT("/api/trips/{tripId}/complete")
    suspend fun completeTrip(@Path("tripId") tripId: String): Response<Trip>
    
    @PUT("/api/trips/{tripId}/progress")
    suspend fun updateTripProgress(
        @Path("tripId") tripId: String,
        @Body request: UpdateProgressRequest
    ): Response<Trip>
    
    @GET("/api/trips/active/{username}")
    suspend fun getActiveTrip(@Path("username") username: String): Response<Trip>
    
    @GET("/api/trips/history/{username}")
    suspend fun getTripHistory(@Path("username") username: String): Response<List<Trip>>
    
    @GET("/api/trips/{tripId}")
    suspend fun getTripById(@Path("tripId") tripId: String): Response<Trip>
}

data class StartTripRequest(
    val username: String,
    val startLocation: String,
    val endLocation: String,
    val startCoordinates: Coordinates?,
    val endCoordinates: Coordinates?,
    val route: Route
)

data class UpdateProgressRequest(
    val currentLegIndex: Int
)