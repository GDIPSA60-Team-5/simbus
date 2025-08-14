package iss.nus.edu.sg.appfiles.feature_notification.api

import iss.nus.edu.sg.appfiles.feature_notification.api.NotificationApi
import iss.nus.edu.sg.appfiles.feature_notification.api.NotificationStatusRequest
import iss.nus.edu.sg.appfiles.feature_notification.api.NotificationStatusResponse
import javax.inject.Inject

class NotificationController @Inject constructor(
    private val api: NotificationApi
) {
    suspend fun updateStatus(notificationId: Int, status: String): Result<NotificationStatusResponse> {
        return try {
            val response = api.updateNotificationStatus(notificationId, NotificationStatusRequest(status))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
