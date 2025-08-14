package com.example.feature_trip_notification

import android.content.Context
import androidx.work.*
import com.example.core.api.BusApi
import com.example.core.api.UserApi
import com.example.core.model.BusArrival
import com.example.core.model.RouteLeg
import com.example.core.model.Trip
import com.example.core.service.TripService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to monitor bus arrivals for active trips and send notifications
 * when buses are approaching for the next leg of the journey
 */
@Singleton
class BusArrivalNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripService: TripService,
    private val userApi: UserApi,
    private val busApi: BusApi,
    private val tripNotificationManager: TripNotificationManager
) {
    
    companion object {
        private const val WORK_NAME = "bus_arrival_monitoring"
        private const val CHECK_INTERVAL_MINUTES = 2L // Check every 2 minutes
        private const val NOTIFICATION_THRESHOLD_MINUTES = 5L // Notify when bus is ≤ 5 minutes away
    }
    
    // Track last notification to prevent spam
    private var lastNotificationKey: String? = null
    private var lastNotificationTime: Long = 0
    
    /**
     * Starts monitoring bus arrivals for active trips
     */
    fun startBusArrivalMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val monitoringRequest = PeriodicWorkRequestBuilder<BusArrivalWorker>(
            CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                monitoringRequest
            )
    }
    
    /**
     * Stops bus arrival monitoring
     */
    fun stopBusArrivalMonitoring() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }
    
    /**
     * Manually check bus arrivals for current trip (used when trip progresses)
     */
    suspend fun checkBusArrivalsForCurrentTrip() {
        try {
            // Get current user
            val userResponse = userApi.getCurrentUser()
            if (!userResponse.isSuccessful || userResponse.body() == null) {
                return
            }
            
            val username = userResponse.body()!!.username
            
            // Get active trip
            val activeTrip = tripService.getActiveTrip(username).getOrNull()
            if (activeTrip != null) {
                checkAndNotifyBusArrivals(activeTrip)
            }
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("BusArrivalNotification", "Error checking bus arrivals", e)
        }
    }
    
    private suspend fun checkAndNotifyBusArrivals(trip: Trip) {
        val route = trip.route
        val currentLegIndex = trip.currentLegIndex
        
        // Check if current leg is WALK and next leg is BUS
        if (currentLegIndex < route.legs.size) {
            val currentLeg = route.legs[currentLegIndex]
            val nextLegIndex = currentLegIndex + 1
            
            if (currentLeg.type.uppercase() == "WALK" && 
                nextLegIndex < route.legs.size) {
                
                val nextLeg = route.legs[nextLegIndex]
                if (nextLeg.type.uppercase() == "BUS" && 
                    currentLeg.toStopName != null && 
                    nextLeg.busServiceNumber != null) {
                    
                    // Get bus arrivals for the bus stop and service
                    try {
                        val response = busApi.getBusArrivals(
                            currentLeg.toStopName!!, 
                            nextLeg.busServiceNumber
                        )
                        
                        if (response.isSuccessful && response.body() != null) {
                            val busArrivals = response.body()!!
                            processBusArrivals(busArrivals, nextLeg, currentLeg.toStopName!!)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BusArrivalNotification", "Error fetching bus arrivals", e)
                    }
                }
            }
        }
    }
    
    private fun processBusArrivals(busArrivals: List<BusArrival>, nextLeg: RouteLeg, busStopName: String) {
        val targetService = nextLeg.busServiceNumber ?: return
        
        // Find the specific bus service we need
        val relevantArrival = busArrivals.find { it.serviceName == targetService }
        if (relevantArrival != null && relevantArrival.arrivals.isNotEmpty()) {
            
            try {
                val now = OffsetDateTime.now()
                val firstArrival = OffsetDateTime.parse(relevantArrival.arrivals[0])
                val minutesUntilArrival = ChronoUnit.MINUTES.between(now, firstArrival)
                
                // Send notification if bus is arriving soon
                if (minutesUntilArrival in 1..NOTIFICATION_THRESHOLD_MINUTES) {
                    val arrivalText = when {
                        minutesUntilArrival <= 0 -> "Now"
                        minutesUntilArrival == 1L -> "1 min"
                        else -> "${minutesUntilArrival} min"
                    }
                    
                    // Create unique key to prevent duplicate notifications
                    val notificationKey = "${targetService}_${busStopName}_${minutesUntilArrival}"
                    val currentTime = System.currentTimeMillis()
                    
                    // Only send notification if it's different from last one or enough time has passed (5+ minutes)
                    if (lastNotificationKey != notificationKey || 
                        (currentTime - lastNotificationTime) > (5 * 60 * 1000)) {
                        
                        tripNotificationManager.showBusArrivalNotification(
                            busService = targetService,
                            busStop = busStopName,
                            arrivalTime = arrivalText
                        )
                        
                        lastNotificationKey = notificationKey
                        lastNotificationTime = currentTime
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("BusArrivalNotification", "Error parsing arrival time", e)
            }
        }
    }
}

/**
 * Worker class that performs the bus arrival monitoring
 * Uses manual dependency resolution to avoid Hilt complexity with WorkManager
 */
class BusArrivalWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Get the service manually from the application context
            val app = applicationContext as? dagger.hilt.android.HiltAndroidApp
            if (app != null) {
                val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                    applicationContext,
                    BusArrivalWorkerEntryPoint::class.java
                )
                entryPoint.getBusArrivalNotificationService().checkBusArrivalsForCurrentTrip()
            }
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("BusArrivalWorker", "Error in bus arrival monitoring", e)
            Result.retry()
        }
    }
}

/**
 * Entry point for accessing Hilt dependencies in WorkManager
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface BusArrivalWorkerEntryPoint {
    fun getBusArrivalNotificationService(): BusArrivalNotificationService
}