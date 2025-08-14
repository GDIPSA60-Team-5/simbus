package com.example.core.api

import retrofit2.Response
import retrofit2.http.GET
import java.time.LocalTime

interface CommuteApi {
    @GET("/api/user/commute-plans/me")
    suspend fun getMyCommutes(): Response<List<CommutePlan>>
}

data class CommutePlan(
    val id: String,
    val commutePlanName: String,
    val notifyAt: String,
    val arrivalTime: String,
    val reminderOffsetMin: Int?,
    val recurrence: Boolean?,
    val startLocationId: String?,
    val endLocationId: String?,
    val userId: String,
    val commuteHistoryIds: List<String>?,
    val preferredRouteIds: List<String>?,
    val commuteRecurrenceDayIds: List<String>?
)