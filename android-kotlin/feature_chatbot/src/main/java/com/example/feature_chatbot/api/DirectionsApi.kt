package com.example.feature_chatbot.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DirectionsApi {
    suspend fun getResponseFor(input: String): String
}

class DummyDirectionsApi : DirectionsApi {
    override suspend fun getResponseFor(input: String): String = withContext(Dispatchers.Default) {
        when {
            input.contains("directions", ignoreCase = true) -> "Fetching directions for you."
            input.contains("hello", ignoreCase = true) -> "Hello! How can I assist you?"
            else -> "Sorry, I didn't get that. Can you try again?"
        }
    }
}
