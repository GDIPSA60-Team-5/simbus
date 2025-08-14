package com.example.feature_chatbot.data

import com.example.core.model.Coordinates

data class ChatRequest(
    val userInput: String,
    val currentLocation: Coordinates?,
    val currentTimestamp: Long 
)