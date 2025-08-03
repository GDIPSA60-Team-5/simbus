package com.example.feature_chatbot.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.BotResponseTypeAdapter
import java.util.concurrent.TimeUnit

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BotResponse::class.java, BotResponseTypeAdapter())
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)   // Time to connect to server
        .readTimeout(60, TimeUnit.SECONDS)      // Time to wait for server response
        .writeTimeout(30, TimeUnit.SECONDS)     // Time to send request to server
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val chatbotApi: ChatbotApi = retrofit.create(ChatbotApi::class.java)
}
