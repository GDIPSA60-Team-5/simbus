package com.example.core.api

import retrofit2.Response
import retrofit2.http.*

interface LocationApi {
    
    @GET("/api/locations")
    suspend fun getUserLocations(): Response<List<SavedLocation>>
    
    @POST("/api/locations")
    suspend fun addLocation(@Body request: CreateLocationRequest): Response<SavedLocation>
    
    @PUT("/api/locations/{locationId}")
    suspend fun updateLocation(
        @Path("locationId") locationId: String,
        @Body request: CreateLocationRequest
    ): Response<SavedLocation>
    
    @DELETE("/api/locations/{locationId}")
    suspend fun deleteLocation(@Path("locationId") locationId: String): Response<Unit>
    
    @GET("/api/locations/{locationId}")
    suspend fun getLocation(@Path("locationId") locationId: String): Response<SavedLocation>
    
    @GET("/api/geocode")
    suspend fun searchLocations(@Query("locationName") query: String): Response<GeocodeResponse>
    
    @POST("/api/routing")
    suspend fun getRouteOptions(@Body request: RoutingRequest): Response<DirectionsResponse>
}

data class SavedLocation(
    val id: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val userId: String
)

data class CreateLocationRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class GeocodeResponse(
    val found: Int,
    val pageNum: Int,
    val totalNumPages: Int,
    val results: List<GeocodeCandidate>
)

data class GeocodeCandidate(
    val latitude: String,
    val longitude: String,
    val displayName: String,
    val postalCode: String,
    val block: String,
    val road: String,
    val building: String
)

data class RoutingRequest(
    val startCoordinates: String,
    val endCoordinates: String,
    val startLocation: String,
    val endLocation: String,
    val arrivalTime: String? = null,
    val startTime: String? = null
)

data class DirectionsResponse(
    val startLocation: String,
    val endLocation: String,
    val startCoordinates: com.example.core.model.Coordinates,
    val endCoordinates: com.example.core.model.Coordinates,
    val suggestedRoutes: List<com.example.core.model.Route>
)