package com.example.feature_chatbot.data

data class Route(
    val summary: String,
    val durationInMinutes: Int,
    val routeGeometry: String?,
    val legs: List<RouteStep>
)
