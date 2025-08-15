package iss.nus.edu.sg.appfiles.feature_login.api

import iss.nus.edu.sg.appfiles.feature_login.data.LoginRequest
import iss.nus.edu.sg.appfiles.feature_login.data.RegisterRequest
import iss.nus.edu.sg.appfiles.feature_login.data.AuthResponse
import iss.nus.edu.sg.appfiles.feature_login.data.MessageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthController @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = authApi.login(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<MessageResponse> {
        return try {
            val response = authApi.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
