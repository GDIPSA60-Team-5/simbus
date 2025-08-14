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
import iss.nus.edu.sg.feature_notification.R
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import java.time.Duration
import java.time.ZonedDateTime

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

        val busJson = message.data["nextBus"] ?: "[]"
        Log.d("FCM", "busJson=$busJson")

        val nextBusInfo = try {
            val busList = org.json.JSONArray(busJson)
            if (busList.length() == 0) "" else {
                val firstBus = busList.getJSONObject(0)
                val serviceName = firstBus.getString("serviceName")
                val arrivals = firstBus.getJSONArray("arrivals")
                val now = ZonedDateTime.now()

                (0 until minOf(3, arrivals.length())).joinToString("\n") { index ->
                    val isoStr = arrivals.getString(index)
                    val arrivalTime = ZonedDateTime.parse(isoStr)
                    val minutesDiff = Duration.between(now, arrivalTime).toMinutes()
                    val timeText = if (minutesDiff <= 0) "Arriving now" else "In $minutesDiff min"
                    "$serviceName: $timeText"
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Failed parsing bus arrivals", e)
            ""
        }.trim()
        Log.d("FCM", "NextBusInfo=$nextBusInfo")

        if (nextBusInfo.isNotEmpty()) {
            sendNotification(nextBusInfo)
        } else {
            Log.w("FCM", "NextBusInfo is empty, skipping notification")
        }
    }

    @SuppressLint("ServiceCast")
    private fun sendNotification(nextBusInfo: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bus_alert_channel_v2"
        val channelName = "Bus Alert Channel"

        // Check if notifications are enabled
        if (!manager.areNotificationsEnabled()) {
            Log.w("FCM", "Notifications are disabled for this app")
            return
        }

        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for bus arrival alerts"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            Log.d("FCM", "Created notification channel: $channelId")
        }

        // Try to use app icon, fallback to system icon if not available
        val iconRes = try {
            R.drawable.ic_bus_notification
        } catch (e: Exception) {
            Log.w("FCM", "App icon not found, using system icon")
            android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle("Bus Alert")
            .setContentText(nextBusInfo.take(50)) // optional short text for lock screen
            .setStyle(NotificationCompat.BigTextStyle().bigText(nextBusInfo))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationId = System.currentTimeMillis().toInt()
        Log.d("FCM", "Sending notification with ID $notificationId, content: '$nextBusInfo'")
        
        try {
            manager.notify(notificationId, builder.build())
            Log.d("FCM", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("FCM", "Failed to send notification", e)
        }
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
