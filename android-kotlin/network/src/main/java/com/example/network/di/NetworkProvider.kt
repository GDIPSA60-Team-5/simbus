package com.example.core.di

import com.example.core.di.SecureStorageManager
import com.example.network.di.AuthInterceptor
import com.example.core.api.UserApi
import com.example.core.api.CommuteApi
import com.example.core.api.TripApi
import com.example.core.api.BusApi
import com.example.feature_chatbot.api.ChatbotApi
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.BotResponseTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import iss.nus.edu.sg.appfiles.feature_login.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import com.example.network.BuildConfig
import iss.nus.edu.sg.appfiles.feature_navigatebar.FeedbackApi
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvider {
    @Provides
    @Singleton
    fun provideAuthInterceptor(secureStorageManager: SecureStorageManager): AuthInterceptor =
        AuthInterceptor { secureStorageManager.getToken() }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideCommuteApi(retrofit: Retrofit): CommuteApi =
        retrofit.create(CommuteApi::class.java)

    @Provides
    @Singleton
    fun provideTripApi(retrofit: Retrofit): TripApi =
        retrofit.create(TripApi::class.java)

    @Provides
    @Singleton
    fun provideBusApi(retrofit: Retrofit): BusApi =
        retrofit.create(BusApi::class.java)

    // Chatbot provider
    @Provides
    @Singleton
    fun provideCustomGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(BotResponse::class.java, BotResponseTypeAdapter())
            .create()

    @Provides
    @Singleton
    @Named("chatbot")
    fun provideChatbotRetrofit(
        client: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("chatbot")
    fun provideChatbotApi(@Named("chatbot") retrofit: Retrofit): ChatbotApi =
        retrofit.create(ChatbotApi::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApi =
        retrofit.create(FeedbackApi::class.java)
}


