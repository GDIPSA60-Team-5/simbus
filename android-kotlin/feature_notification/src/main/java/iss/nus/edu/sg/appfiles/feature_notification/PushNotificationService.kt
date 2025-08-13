package iss.nus.edu.sg.appfiles.feature_notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import iss.nus.edu.sg.appfiles.feature_notification.api.DeviceTokenController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenController: DeviceTokenController

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message data payload: ${message.data}")
        val notification = message.notification
        val title = notification?.title ?: message.data["title"]
        val body = notification?.body ?: message.data["body"]
        sendNotification(title, body)
    }

    @SuppressLint("ServiceCast")
    private fun sendNotification(title: String?, body: String?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel_id"
        val channelName = "Default Channel"
        manager.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH))
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title ?: "Notification")
            .setContentText(body ?: "")
            .setAutoCancel(true)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun sendTokenToServer(token: String) {
        val deviceId = DeviceIdUtil.getDeviceId(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = deviceTokenController.updateDeviceToken(deviceId, token)
                result.onSuccess {
                    Log.d("PushNotificationService", "Token sent successfully: ${it.message}")
                    savePendingToken(applicationContext, null)
                }.onFailure {
                    Log.e("PushNotificationService", "Failed to send token", it)
                    savePendingToken(applicationContext, token)
                }
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Exception sending token", e)
                savePendingToken(applicationContext, token)
            }
        }
    }

    companion object {
        fun savePendingToken(context: Context, token: String?) {
            val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
            prefs.edit { putString("pending_token", token) }
        }

        fun retryPendingToken(context: Context, controller: DeviceTokenController) {
            val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
            val pending = prefs.getString("pending_token", null)
            if (!pending.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val deviceId = DeviceIdUtil.getDeviceId(context)
                        val result = controller.updateDeviceToken(deviceId, pending)
                        result.onSuccess {
                            Log.d("PushNotificationService", "Pending token sent: ${it.message}")
                            prefs.edit { remove("pending_token") }
                        }.onFailure {
                            Log.e("PushNotificationService", "Failed to send pending token", it)
                        }
                    } catch (e: Exception) {
                        Log.e("PushNotificationService", "Exception sending pending token", e)
                    }
                }
            }
        }

        fun logSavedToken(context: Context) {
            val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
            val pending = prefs.getString("pending_token", null)
            Log.d("FCM", "Saved pending token: $pending")
        }
    }
}
