package iss.nus.edu.sg.appfiles.feature_notification.api

import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

data class NotificationStatusRequest(val status: String)
data class NotificationStatusResponse(val message: String)

interface NotificationApi {
    @PUT("api/notifications/{notificationId}/status")
    suspend fun updateNotificationStatus(
        @Path("notificationId") notificationId: Int,
        @Body request: NotificationStatusRequest
    ): NotificationStatusResponse
}
