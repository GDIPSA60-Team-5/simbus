package com.example.feature_chatbot.api

import com.example.feature_chatbot.data.BotResponse

import retrofit2.http.GET
import retrofit2.http.Query

interface ChatbotApi {
    @GET("api/chatbot")
    suspend fun getResponseFor(@Query("input") input: String): BotResponse
}
