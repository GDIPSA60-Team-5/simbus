package iss.nus.edu.sg.appfiles.feature_notification.api

import retrofit2.http.Body
import retrofit2.http.POST

data class DeviceTokenRequest(
    val deviceId: String,
    val fcmToken: String
)

data class DeviceTokenResponse(
    val message: String
)

interface DeviceTokenApi {
    @POST("api/device-token")
    suspend fun updateDeviceToken(@Body request: DeviceTokenRequest): DeviceTokenResponse
}
