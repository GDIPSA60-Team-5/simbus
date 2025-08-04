package iss.nus.edu.sg.appfiles.feature_login.api

import iss.nus.edu.sg.appfiles.feature_login.model.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.model.AuthResponse
import iss.nus.edu.sg.appfiles.feature_login.model.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    fun login(@Body request: AuthRequest): Call<AuthResponse>

    @POST("api/auth/register")
    fun register(@Body request: AuthRequest): Call<MessageResponse>
}
