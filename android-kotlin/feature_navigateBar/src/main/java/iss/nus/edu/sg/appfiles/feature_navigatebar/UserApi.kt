package iss.nus.edu.sg.appfiles.feature_navigatebar
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("user/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Unit>
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)