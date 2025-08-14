package com.example.core.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.http.*

interface CommuteApi {
    @GET("/api/user/commute-plans/me")
    suspend fun getMyCommutes(): Response<List<CommutePlan>>
    
    @POST("/api/user/commute-plans")
    suspend fun createCommutePlan(@Body request: CreateCommutePlanRequest): Response<CommutePlan>
    
    @PUT("/api/user/commute-plans/{id}")
    suspend fun updateCommutePlan(
        @Path("id") id: String,
        @Body request: UpdateCommutePlanRequest
    ): Response<CommutePlan>
    
    @DELETE("/api/user/commute-plans/{id}")
    suspend fun deleteCommutePlan(@Path("id") id: String): Response<Unit>
    
    @GET("/api/user/commute-plans/{id}")
    suspend fun getCommutePlan(@Path("id") id: String): Response<CommutePlan>
    
    @POST("/api/user/commute-plans/{id}/preferred-routes")
    suspend fun addPreferredRoute(
        @Path("id") commutePlanId: String,
        @Body request: CreatePreferredRouteRequest
    ): Response<PreferredRoute>
    
    @GET("/api/user/commute-plans/{id}/preferred-routes")
    suspend fun getPreferredRoutes(@Path("id") commutePlanId: String): Response<List<PreferredRoute>>
    
    @POST("/api/saved-trip-routes")
    suspend fun createSavedTripRoute(@Body request: CreateSavedTripRouteRequest): Response<SavedTripRoute>
}

@Parcelize
data class CommutePlan(
    val id: String,
    val commutePlanName: String,
    val notifyAt: String,
    val arrivalTime: String?,
    val reminderOffsetMin: Int?,
    val recurrence: Boolean?,
    val startLocationId: String?,
    val endLocationId: String?,
    val userId: String,
    val savedTripRouteId: String?,
    val commuteHistoryIds: List<String>?,
    val preferredRouteIds: List<String>?,
    val commuteRecurrenceDayIds: List<String>?
) : Parcelable

data class CreateCommutePlanRequest(
    val commutePlanName: String,
    val notifyAt: String,
    val arrivalTime: String?,
    val reminderOffsetMin: Int?,
    val recurrence: Boolean?,
    val startLocationId: String,
    val endLocationId: String,
    val savedTripRouteId: String? = null,
    val commuteRecurrenceDayIds: List<String>? = null
)

data class UpdateCommutePlanRequest(
    val commutePlanName: String?,
    val notifyAt: String?,
    val arrivalTime: String?,
    val reminderOffsetMin: Int?,
    val recurrence: Boolean?,
    val startLocationId: String?,
    val endLocationId: String?,
    val savedTripRouteId: String? = null,
    val commuteRecurrenceDayIds: List<String>? = null
)

data class PreferredRoute(
    val id: String,
    val commutePlanId: String,
    val routeId: String
)

data class CreatePreferredRouteRequest(
    val routeId: String
)

data class SavedTripRoute(
    val id: String,
    val routeData: com.example.core.model.Route,
    val userId: String
)

data class CreateSavedTripRouteRequest(
    val routeData: com.example.core.model.Route
)