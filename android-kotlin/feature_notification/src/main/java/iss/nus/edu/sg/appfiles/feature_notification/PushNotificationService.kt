package iss.nus.edu.sg.appfiles.feature_notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import iss.nus.edu.sg.appfiles.feature_notification.api.DeviceTokenController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_notification.broadcast_receiver.NotificationHelper
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
        val busStopName = message.data["busStopName"] ?: "Bus Stop"
        val notificationIdStr = message.data["notificationId"] ?: ""
        val notificationId = notificationIdStr.toIntOrNull() ?: -1
        Log.d("FCM", "Using notificationId=$notificationId from backend")

        val nextBusInfo = try {
            val busList = org.json.JSONArray(busJson)
            if (busList.length() == 0) "" else {
                val lines = mutableListOf<String>()
                lines.add(busStopName) // first line: bus stop name

                for (i in 0 until busList.length()) {
                    val bus = busList.getJSONObject(i)
                    val serviceName = bus.getString("serviceName")
                    val arrivals = bus.getJSONArray("arrivals")
                    val now = ZonedDateTime.now()

                    if (arrivals.length() > 0) {
                        val timesText = (0 until minOf(3, arrivals.length()))
                            .map { index ->
                                val arrivalTime = ZonedDateTime.parse(arrivals.getString(index))
                                val minutesDiff = Duration.between(now, arrivalTime).toMinutes()
                                if (minutesDiff <= 0) "Arriving now" else "$minutesDiff min"
                            }
                            .joinToString(", ") // all arrivals for the same service on one line
                        lines.add("$serviceName: $timesText")
                    } else {
                        lines.add("$serviceName: No upcoming arrival")
                    }
                }

                lines.joinToString("\n")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Failed parsing bus arrivals", e)
            ""
        }.trim()

        if (nextBusInfo.isNotEmpty()) {
            sendNotification(notificationId, nextBusInfo)
        } else {
            Log.w("FCM", "NextBusInfo is empty, skipping notification")
        }
    }

    @SuppressLint("ServiceCast")
    private fun sendNotification(notificationId: Int, nextBusInfo: String) {
        val prefs = getSharedPreferences("bus_notifications", Context.MODE_PRIVATE)
        val isMuted = prefs.getBoolean("muted_$notificationId", false)

        // Save the latest bus info for this notification ID
        prefs.edit().putString("nextBusInfo_$notificationId", nextBusInfo).apply()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationHelper.buildNotification(
            this,
            notificationId,
            nextBusInfo,
            muted = isMuted
        )
        manager.notify(notificationId, builder.build())
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
