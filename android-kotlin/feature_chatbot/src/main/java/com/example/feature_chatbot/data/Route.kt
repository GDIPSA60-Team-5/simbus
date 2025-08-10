package com.example.feature_chatbot.data

data class Route(
    val durationInMinutes: Int,
    val legs: List<RouteLeg>,
    val summary: String,
)

data class RouteLeg(
    val type: String,
    val durationInMinutes: Int,
    val busServiceNumber: String?,
    val instruction: String,
    val legGeometry: String
)