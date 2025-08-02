package com.example.feature_chatbot.api

import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.ChatRequest
import retrofit2.http.Body

import retrofit2.http.POST
import retrofit2.http.Query

interface ChatbotApi {
    @POST("api/chatbot")
    suspend fun getResponseFor(@Body request: ChatRequest): BotResponse
}
