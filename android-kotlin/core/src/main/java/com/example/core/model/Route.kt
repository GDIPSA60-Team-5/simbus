package com.example.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Route(
    val durationInMinutes: Int,
    val legs: List<RouteLeg>,
    val summary: String,
) : Parcelable

@Parcelize
data class RouteLeg(
    val type: String,
    val durationInMinutes: Int,
    val busServiceNumber: String?,
    val instruction: String,
    val legGeometry: String,
    val routePoints: List<Coordinates>?,
    val fromStopName: String?,
    val fromStopCode: String?,
    val toStopName: String?,
    val toStopCode: String?
) : Parcelable