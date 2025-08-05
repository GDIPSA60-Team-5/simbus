package iss.nus.edu.sg.appfiles.feature_login.api

import iss.nus.edu.sg.appfiles.feature_login.data.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.data.AuthResponse
import iss.nus.edu.sg.appfiles.feature_login.data.MessageResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): MessageResponse
}
