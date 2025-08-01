package com.example.feature_chatbot.data

data class RouteStep(
    val type: String, // "WALK", "BUS", etc.
    val instruction: String,
    val busServiceNumber: String?,
    val durationInMinutes: Int
)