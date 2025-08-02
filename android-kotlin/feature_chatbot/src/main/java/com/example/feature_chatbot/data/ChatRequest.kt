package com.example.feature_chatbot.data

data class ChatRequest(
    val userInput: String,
    val currentLocation: Coordinates?,
    val currentTimestamp: Long // Using Long for milliseconds since epoch
)