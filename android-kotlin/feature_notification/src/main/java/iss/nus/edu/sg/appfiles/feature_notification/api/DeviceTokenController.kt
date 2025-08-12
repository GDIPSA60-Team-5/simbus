package iss.nus.edu.sg.appfiles.feature_notification.api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenController @Inject constructor(
    private val deviceTokenApi: DeviceTokenApi
) {

    suspend fun updateDeviceToken(deviceId: String, fcmToken: String): Result<DeviceTokenResponse> {
        return try {
            val response = deviceTokenApi.updateDeviceToken(
                DeviceTokenRequest(deviceId, fcmToken)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
