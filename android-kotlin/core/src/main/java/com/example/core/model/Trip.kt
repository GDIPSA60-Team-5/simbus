package com.example.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trip(
    val id: String,
    val username: String,
    val startLocation: String,
    val endLocation: String,
    val startCoordinates: Coordinates?,
    val endCoordinates: Coordinates?,
    val route: Route,
    val status: TripStatus,
    val currentLegIndex: Int,
    val startTime: String,
    val endTime: String?
) : Parcelable

enum class TripStatus {
    ON_TRIP,
    COMPLETED
}