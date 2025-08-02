package com.example.feature_chatbot.api

import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.ChatRequest
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.Query

interface ChatbotApi {
    @GET("api/chatbot")
    suspend fun getResponseFor(@Body request: ChatRequest): BotResponse
}
