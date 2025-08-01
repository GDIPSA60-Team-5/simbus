package com.example.feature_chatbot.data

data class DirectionsResponse(
    val startLocation: String?,
    val endLocation: String?,
    val suggestedRoutes: List<Route>?
)