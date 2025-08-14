package com.example.feature_notification

import android.content.Context
import androidx.work.*
import com.example.core.api.UserApi
import com.example.core.api.UserNotification
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service to poll notifications from server and display them as local notifications
 */
class NotificationPollingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userApi: UserApi,
    private val secureStorageManager: SecureStorageManager,
    private val tripNotificationManager: TripNotificationManager
) {
    
    companion object {
        private const val WORK_NAME = "notification_polling"
        private const val POLL_INTERVAL_MINUTES = 1L
    }
    
    /**
     * Starts periodic notification polling
     */
    fun startPolling() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val pollingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            POLL_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                pollingRequest
            )
    }
    
    /**
     * Stops notification polling
     */
    fun stopPolling() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }
}

/**
 * Worker class that performs the actual notification polling
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    @Inject
    lateinit var userApi: UserApi
    
    @Inject
    lateinit var secureStorageManager: SecureStorageManager
    
    @Inject
    lateinit var tripNotificationManager: TripNotificationManager
    
    // Set to track processed notifications to avoid duplicates
    private val processedNotifications = mutableSetOf<String>()
    
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val token = secureStorageManager.getToken()
            if (token == null) {
                return@coroutineScope Result.failure()
            }
            
            // Get current user
            val userResponse = userApi.getCurrentUser()
            if (!userResponse.isSuccessful || userResponse.body() == null) {
                return@coroutineScope Result.failure()
            }
            
            val userId = userResponse.body()!!.id
            
            // Get notifications for this user
            val notificationsResponse = userApi.getUserNotifications(userId)
            if (!notificationsResponse.isSuccessful || notificationsResponse.body() == null) {
                return@coroutineScope Result.success()
            }
            
            val notifications = notificationsResponse.body()!!
            
            // Process new notifications
            notifications.forEach { notification ->
                if (!processedNotifications.contains(notification.id)) {
                    processNotification(notification)
                    processedNotifications.add(notification.id)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun processNotification(notification: UserNotification) {
        when (notification.type) {
            "TRIP_START" -> {
                // Parse trip information from the message if needed
                tripNotificationManager.showTripStartNotification(
                    startLocation = "Starting point",
                    endLocation = "Destination"
                )
            }
            "TRIP_INSTRUCTION" -> {
                tripNotificationManager.showInstructionNotification(notification.message)
            }
            "BUS_ARRIVAL" -> {
                // Extract bus information from the message
                tripNotificationManager.showBusArrivalNotification(
                    busService = "Bus",
                    busStop = "Stop",
                    arrivalTime = "Soon"
                )
            }
        }
    }
}

