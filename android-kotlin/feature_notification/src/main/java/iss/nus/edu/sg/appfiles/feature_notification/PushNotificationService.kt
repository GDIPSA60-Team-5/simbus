package iss.nus.edu.sg.appfiles.feature_notification

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
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
        Log.d("FCM", "Refreshed token: $token")
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
                    savePendingToken(null) // clear saved token
                }.onFailure {
                    Log.e("PushNotificationService", "Failed to send token", it)
                    savePendingToken(token) // save token for retry
                }
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Exception sending token", e)
                savePendingToken(token) // save token for retry
            }
        }
    }

    private fun savePendingToken(token: String?) {
        val prefs = getSharedPreferences("push_prefs", MODE_PRIVATE)
        prefs.edit().putString("pending_token", token).apply()
    }

    // Call this at app startup to retry
    fun retryPendingToken(context: Context) {
        val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
        val pending = prefs.getString("pending_token", null)
        if (!pending.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = DeviceIdUtil.getDeviceId(context)
                    val result = deviceTokenController.updateDeviceToken(deviceId, pending)
                    result.onSuccess {
                        Log.d("DeviceTokenManager", "Token sent successfully: ${it.message}")
                        prefs.edit().remove("pending_token").apply()
                    }.onFailure {
                        Log.e("DeviceTokenManager", "Failed to send token", it)
                    }
                } catch (e: Exception) {
                    Log.e("DeviceTokenManager", "Exception sending token", e)
                }
            }
        }
    }
}