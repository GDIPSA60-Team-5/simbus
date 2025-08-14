package com.example.feature_notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID = "trip_notifications"
        private const val CHANNEL_NAME = "Trip Navigation"
        private const val CHANNEL_DESCRIPTION = "Notifications for active trip navigation"
        
        // Notification IDs
        private const val TRIP_START_NOTIFICATION_ID = 1001
        private const val INSTRUCTION_NOTIFICATION_ID = 1002
        private const val BUS_ARRIVAL_NOTIFICATION_ID = 1003
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows trip start notification
     */
    fun showTripStartNotification(startLocation: String, endLocation: String) {
        if (!hasNotificationPermission()) return
        
        // Create generic intent to main activity - will be resolved at runtime
        val intent = Intent().apply {
            setClassName(context, "com.example.busappkotlin.ui.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // You can replace with app icon
            .setContentTitle("Trip Starting")
            .setContentText("Trip starting from $startLocation to $endLocation")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(TRIP_START_NOTIFICATION_ID, notification)
    }
    
    /**
     * Shows first instruction notification
     */
    fun showInstructionNotification(instruction: String) {
        if (!hasNotificationPermission()) return
        
        // Create generic intent to main activity - will be resolved at runtime
        val intent = Intent().apply {
            setClassName(context, "com.example.busappkotlin.ui.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Next Step")
            .setContentText(instruction)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(INSTRUCTION_NOTIFICATION_ID, notification)
    }
    
    /**
     * Shows bus arrival notification
     */
    fun showBusArrivalNotification(busService: String, busStop: String, arrivalTime: String) {
        if (!hasNotificationPermission()) return
        
        // Create generic intent to main activity - will be resolved at runtime
        val intent = Intent().apply {
            setClassName(context, "com.example.busappkotlin.ui.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationText = if (arrivalTime == "Now") {
            "Bus $busService is arriving now at $busStop!"
        } else {
            "Bus $busService arriving at $busStop in $arrivalTime"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // You can use R.drawable.ic_bus if available
            .setContentTitle("🚌 Your Bus is Coming!")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(if (arrivalTime == "Now" || arrivalTime == "1 min") 
                NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(if (arrivalTime == "Now" || arrivalTime == "1 min") 
                longArrayOf(0, 500, 250, 500) else null)
            .build()
        
        NotificationManagerCompat.from(context).notify(BUS_ARRIVAL_NOTIFICATION_ID, notification)
    }
    
    /**
     * Shows trip completion notification
     */
    fun showTripCompletionNotification() {
        if (!hasNotificationPermission()) return
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Trip Completed")
            .setContentText("You have reached your destination!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(TRIP_START_NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancels all trip-related notifications
     */
    fun cancelAllTripNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(TRIP_START_NOTIFICATION_ID)
        notificationManager.cancel(INSTRUCTION_NOTIFICATION_ID)
        notificationManager.cancel(BUS_ARRIVAL_NOTIFICATION_ID)
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}