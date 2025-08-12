package iss.nus.edu.sg.appfiles.feature_notification.data

data class DeviceTokenRequest(
    val deviceId: String,
    val fcmToken: String
)

data class DeviceTokenResponse(
    val message: String
)
