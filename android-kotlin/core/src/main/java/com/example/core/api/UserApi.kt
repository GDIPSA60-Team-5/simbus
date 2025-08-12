package com.example.core.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @POST("/api/user/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Unit>
    
    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<User>
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class User(
    val username: String,
    val authorities: List<Authority>? = null
)

data class Authority(
    val authority: String
)