package iss.nus.edu.sg.adprojecttest

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class BusAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val serviceNo = intent?.getStringExtra("serviceNo") ?: "Unknown"
        val action = intent?.getStringExtra("action")

        if (action != null) {
            val message = when (action) {
                "Start now" -> "You journey has begun! The next 2 buses will arrive in: "
                "Missed Bus" -> "Oh No! You missed your bus! Your next bus will arrive in: Your new destination arrival time is: "
                "Skip" -> "You will no longer receive notifications for this route today"
                else -> "Unknown choice for bus $serviceNo"
            }

            val followUpNotification = NotificationCompat.Builder(context, "bus_alerts")
                .setSmallIcon(R.drawable.baseline_bus_alert_24)
                .setContentTitle("Follow up Bus Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 10000).toInt(), followUpNotification)
            return
        }

        val startNowIntent = Intent(context, BusAlertReceiver::class.java).apply{
            putExtra("action","Start now")
        }
        val  startNowPending = PendingIntent.getBroadcast(
            context, 1, startNowIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val missedBusIntent = Intent(context, BusAlertReceiver::class.java).apply{
            putExtra("action", "Missed Bus")
        }
        val missedBusPending = PendingIntent.getBroadcast(
            context, 2, missedBusIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skipIntent = Intent(context, BusAlertReceiver::class.java).apply{
            putExtra("action", "Skip")
        }
        val skipPending = PendingIntent.getBroadcast(
            context, 3, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )



        val notification = NotificationCompat.Builder(context, "bus_alerts")
            .setSmallIcon(R.drawable.baseline_bus_alert_24)
            .setContentTitle("Bus Alert")
            .setContentText("Bus $serviceNo is arriving in 10 minutes")
            .addAction(R.drawable.bus, "Start now", startNowPending)
            .addAction(R.drawable.bus, "Missed Bus", missedBusPending)
            .addAction(R.drawable.bus, "Skip", skipPending)

            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(serviceNo.hashCode(), notification)
    }
}