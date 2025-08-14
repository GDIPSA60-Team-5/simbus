package iss.nus.edu.sg.appfiles.feature_notification.broadcast_receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import iss.nus.edu.sg.feature_notification.R

object NotificationHelper {

    private const val CHANNEL_ID_NORMAL = "bus_alert_channel_v2"
    private const val CHANNEL_ID_MUTED = "bus_alert_channel_muted"

    fun buildNotification(
        context: Context,
        notificationId: Int,
        nextBusInfo: String,
        muted: Boolean
    ): NotificationCompat.Builder {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (muted) CHANNEL_ID_MUTED else CHANNEL_ID_NORMAL
        val channelName = if (muted) "Bus Alert (Muted)" else "Bus Alert Channel"

        // Create channel only if it doesn't exist
        if (manager.getNotificationChannel(channelId) == null) {
            val importance = if (muted) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for bus arrival alerts"
                enableLights(!muted)
                enableVibration(!muted)
                setSound(null, null) // Ensure muted channel has no sound
            }
            manager.createNotificationChannel(channel)
        }

        val iconRes = try { R.drawable.ic_bus_notification } catch (e: Exception) { android.R.drawable.ic_dialog_info }

        val muteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MUTE"
            putExtra("notificationId", notificationId)
        }
        val mutePendingIntent = PendingIntent.getBroadcast(
            context, notificationId, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_SKIP"
            putExtra("notificationId", notificationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1000, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle("Bus Alert")
            .setContentText(nextBusInfo.take(50))
            .setStyle(NotificationCompat.BigTextStyle().bigText(nextBusInfo))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(0) // No sound, vibration handled by channel
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                if (muted) R.drawable.ic_unmute else R.drawable.ic_mute,
                if (muted) "Unmute" else "Mute",
                mutePendingIntent
            )
            .addAction(R.drawable.ic_skip, "Skip", skipPendingIntent)
    }
}
