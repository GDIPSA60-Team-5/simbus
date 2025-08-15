package com.example.feature_chatbot.data

import com.example.core.api.CommutePlan
import com.example.core.model.BusArrival
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

    data class CommutePlanResponse(
        val creationSuccess: Boolean,
        val commutePlan: CommutePlan
    ) : BotResponse()

    data class NextBus(
        val stopCode: String,
        val stopName: String,
        val services: List<BusArrival>
    ) : BotResponse()

    data class Error(val message: String) : BotResponse()
}
