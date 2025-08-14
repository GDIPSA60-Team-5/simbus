package iss.nus.edu.sg.appfiles.feature_notification.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_notification.api.NotificationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationController: NotificationController

    override fun onReceive(context: Context, intent: Intent?) {
        val notificationId = intent?.getIntExtra("notificationId", -1) ?: return
        val prefs = context.getSharedPreferences("bus_notifications", Context.MODE_PRIVATE)

        val lastBusInfo = prefs.getString("nextBusInfo_$notificationId", "") ?: ""

        when (intent.action) {
            "ACTION_MUTE" -> {
                val currentlyMuted = prefs.getBoolean("muted_$notificationId", false)
                prefs.edit().putBoolean("muted_$notificationId", !currentlyMuted).apply()
                Log.d("FCM", "Notification $notificationId muted=${!currentlyMuted}")

                // Update notification to reflect mute/unmute with last bus info
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val updatedBuilder = NotificationHelper.buildNotification(
                    context,
                    notificationId,
                    nextBusInfo = lastBusInfo,
                    muted = !currentlyMuted
                )
                manager.notify(notificationId, updatedBuilder.build())
            }
            "ACTION_SKIP" -> {
                Log.d("FCM", "Notification $notificationId skipped")
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notificationId)

                // Launch coroutine to update backend status
                CoroutineScope(Dispatchers.IO).launch {
                    val result = notificationController.updateStatus(notificationId, "PENDING")
                    if (result.isSuccess) {
                        Log.d("FCM", "Notification $notificationId set to PENDING on backend")
                    } else {
                        Log.e("FCM", "Failed to update notification status", result.exceptionOrNull())
                    }
                }
            }
        }
    }
}
