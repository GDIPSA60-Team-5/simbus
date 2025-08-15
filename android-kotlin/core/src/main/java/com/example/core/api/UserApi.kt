package com.example.core.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @POST("/api/user/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Unit>
    
    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    @GET("/api/notifications/{userId}")
    suspend fun getUserNotifications(@Path("userId") userId: String): Response<List<UserNotification>>
    
    @POST("/api/user/fcm-token")
    suspend fun updateFcmToken(@Body body: UpdateFcmTokenRequest): Response<Unit>
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UpdateFcmTokenRequest(
    val fcmToken: String
)

data class User(
    val id: String,
    val username: String,
    val authorities: List<Authority>? = null
)

data class Authority(
    val authority: String
)

data class UserNotification(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val sentAt: String,
    val expiresAt: String?
)