package com.example.feature_chatbot.data

sealed class BotResponse {
    data class Message(val text: String) : BotResponse()

    data class Directions(
        val startLocation: String,
        val endLocation: String,
        val suggestedRoutes: List<Route>? = null
    ) : BotResponse()

    data class Error(val message: String) : BotResponse()
}
