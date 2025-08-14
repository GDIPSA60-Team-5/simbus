package com.example.feature_chatbot.data

import com.example.core.model.Coordinates
import com.example.core.model.Route

sealed class BotResponse {
    data class Message(val message: String) : BotResponse()

    data class Directions(
        val startLocation: String,
        val endLocation: String,
        val startCoordinates: Coordinates,
        val endCoordinates: Coordinates,
        val suggestedRoutes: List<Route>? = null
    ) : BotResponse()

    data class Error(val message: String) : BotResponse()
}
