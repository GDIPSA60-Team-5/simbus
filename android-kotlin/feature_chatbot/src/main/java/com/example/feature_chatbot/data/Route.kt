package com.example.feature_chatbot.data

data class Route(
    val summary: String,
    val durationInMinutes: Int,
    val legs: List<RouteLeg>
)

data class RouteLeg(
    val type: String,
    val instruction: String,
    val busServiceNumber: String?,
    val durationInMinutes: Int,
    val legGeometry: String
)