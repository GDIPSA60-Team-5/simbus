package iss.nus.edu.sg.appfiles.feature_notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_notification.api.DeviceTokenApi
import iss.nus.edu.sg.appfiles.feature_notification.api.DeviceTokenController
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService: FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenController: DeviceTokenController

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save or send this token to your backend server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Respond to received messages
    }

    private fun sendTokenToServer(token: String) {
        val deviceId = DeviceIdUtil.getDeviceId(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = deviceTokenController.updateDeviceToken(deviceId, token)
                result.onSuccess {
                    Log.d("PushNotificationService", "Token sent successfully: ${it.message}")
                }.onFailure {
                    Log.e("PushNotificationService", "Failed to send token", it)
                }
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Exception sending token", e)
            }
        }
    }
}