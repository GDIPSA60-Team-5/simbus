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
        private const val CHECK_INTERVAL_MINUTES = 1L // Check every minute
        private const val NOTIFICATION_THRESHOLD_MINUTES = 5L // Notify when bus is ≤ 5 minutes away
    }
    
    // Track last notification to prevent spam
    private var lastNotificationKey: String? = null
    private var lastNotificationTime: Long = 0
    
    /**
     * Starts monitoring bus arrivals for active trips
     */
    fun startBusArrivalMonitoring() {
        android.util.Log.d("BusArrivalNotification", "Starting bus arrival monitoring...")
        
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
        
        android.util.Log.d("BusArrivalNotification", "Bus arrival monitoring WorkManager task enqueued")
        
        // Also check immediately for testing
        android.util.Log.d("BusArrivalNotification", "Running immediate check for debugging...")
        try {
            // Use a simple thread for immediate testing instead of coroutine
            Thread {
                kotlinx.coroutines.runBlocking {
                    checkBusArrivalsForCurrentTrip()
                }
            }.start()
        } catch (e: Exception) {
            android.util.Log.e("BusArrivalNotification", "Error in immediate check", e)
        }
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
            android.util.Log.d("BusArrivalNotification", "Checking bus arrivals for current trip...")
            
            // Get current user
            val userResponse = userApi.getCurrentUser()
            android.util.Log.d("BusArrivalNotification", "User API response: success=${userResponse.isSuccessful}, code=${userResponse.code()}")
            
            if (!userResponse.isSuccessful || userResponse.body() == null) {
                android.util.Log.w("BusArrivalNotification", "Failed to get current user or user is null")
                return
            }
            
            val username = userResponse.body()!!.username
            android.util.Log.d("BusArrivalNotification", "Current username: $username")
            
            // Get active trip
            val activeTrip = tripService.getActiveTrip(username).getOrNull()
            android.util.Log.d("BusArrivalNotification", "Active trip: ${if (activeTrip != null) "Found trip ${activeTrip.id}" else "No active trip"}")
            
            if (activeTrip != null) {
                android.util.Log.d("BusArrivalNotification", "Checking bus arrivals for trip ${activeTrip.id}")
                checkAndNotifyBusArrivals(activeTrip)
            } else {
                android.util.Log.d("BusArrivalNotification", "No active trip found for user $username")
            }
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("BusArrivalNotification", "Error checking bus arrivals", e)
        }
    }
    
    private suspend fun checkAndNotifyBusArrivals(trip: Trip) {
        val route = trip.route
        val currentLegIndex = trip.currentLegIndex
        
        android.util.Log.d("BusArrivalNotification", "Checking trip: currentLegIndex=$currentLegIndex, totalLegs=${route.legs.size}")
        
        // Check if current leg is WALK and next leg is BUS
        if (currentLegIndex < route.legs.size) {
            val currentLeg = route.legs[currentLegIndex]
            val nextLegIndex = currentLegIndex + 1
            
            android.util.Log.d("BusArrivalNotification", "Current leg: type=${currentLeg.type}, toStopName=${currentLeg.toStopName}")
            
            if (currentLeg.type.uppercase() == "WALK" && 
                nextLegIndex < route.legs.size) {
                
                val nextLeg = route.legs[nextLegIndex]
                android.util.Log.d("BusArrivalNotification", "Next leg: type=${nextLeg.type}, busService=${nextLeg.busServiceNumber}")
                
                if (nextLeg.type.uppercase() == "BUS" && 
                    currentLeg.toStopName != null && 
                    nextLeg.busServiceNumber != null) {
                    
                    android.util.Log.d("BusArrivalNotification", "Getting bus arrivals for stop: ${currentLeg.toStopName}, service: ${nextLeg.busServiceNumber}")
                    
                    // Get bus arrivals for the bus stop and service
                    try {
                        val response = busApi.getBusArrivals(
                            currentLeg.toStopName!!, 
                            nextLeg.busServiceNumber
                        )
                        
                        android.util.Log.d("BusArrivalNotification", "Bus API response: success=${response.isSuccessful}, code=${response.code()}")
                        
                        if (response.isSuccessful && response.body() != null) {
                            val busArrivals = response.body()!!
                            android.util.Log.d("BusArrivalNotification", "Got ${busArrivals.size} bus arrivals")
                            processBusArrivals(busArrivals, nextLeg, currentLeg.toStopName!!)
                        } else {
                            android.util.Log.w("BusArrivalNotification", "Bus API failed or returned null")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BusArrivalNotification", "Error fetching bus arrivals", e)
                    }
                } else {
                    android.util.Log.d("BusArrivalNotification", "Next leg is not BUS or missing data")
                }
            } else {
                android.util.Log.d("BusArrivalNotification", "Current leg is not WALK or no next leg")
            }
        } else {
            android.util.Log.d("BusArrivalNotification", "Current leg index exceeds route legs")
        }
    }
    
    private fun processBusArrivals(busArrivals: List<BusArrival>, nextLeg: RouteLeg, busStopName: String) {
        val targetService = nextLeg.busServiceNumber ?: return
        
        // Find the specific bus service we need
        val relevantArrival = busArrivals.find { it.serviceName == targetService }
        if (relevantArrival != null && relevantArrival.arrivals.isNotEmpty()) {
            
            try {
                val now = OffsetDateTime.now()
                val arrivalTimes = mutableListOf<String>()
                var shouldNotify = false
                
                // Process up to 3 arrivals
                val arrivalsToProcess = relevantArrival.arrivals.take(3)
                
                for (arrivalTimeStr in arrivalsToProcess) {
                    val arrivalTime = OffsetDateTime.parse(arrivalTimeStr)
                    val minutesUntilArrival = ChronoUnit.MINUTES.between(now, arrivalTime)
                    
                    val arrivalText = when {
                        minutesUntilArrival <= 0 -> "Now"
                        minutesUntilArrival == 1L -> "1min"
                        else -> "${minutesUntilArrival}mins"
                    }
                    
                    arrivalTimes.add(arrivalText)
                    
                    // Check if we should notify (if any bus is within threshold)
                    if (minutesUntilArrival in 1..NOTIFICATION_THRESHOLD_MINUTES) {
                        shouldNotify = true
                    }
                }
                
                // Send notification if any bus is arriving soon
                if (shouldNotify && arrivalTimes.isNotEmpty()) {
                    val arrivalTimesText = arrivalTimes.joinToString(", ")
                    
                    // Create unique key to prevent duplicate notifications
                    val notificationKey = "${targetService}_${busStopName}_${arrivalTimesText}"
                    val currentTime = System.currentTimeMillis()
                    
                    // Only send notification if it's different from last one or enough time has passed (using CHECK_INTERVAL timing)
                    if (lastNotificationKey != notificationKey || 
                        (currentTime - lastNotificationTime) > (CHECK_INTERVAL_MINUTES * 60 * 1000)) {
                        
                        tripNotificationManager.showBusArrivalNotification(
                            busService = targetService,
                            busStop = busStopName,
                            arrivalTime = arrivalTimesText
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
            android.util.Log.d("BusArrivalWorker", "Worker started - checking bus arrivals...")
            
            // Get the service manually from the application context
            val app = applicationContext as? dagger.hilt.android.HiltAndroidApp
            android.util.Log.d("BusArrivalWorker", "App context: ${if (app != null) "HiltAndroidApp found" else "Not HiltAndroidApp"}")
            
            if (app != null) {
                val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                    applicationContext,
                    BusArrivalWorkerEntryPoint::class.java
                )
                android.util.Log.d("BusArrivalWorker", "Entry point created, calling checkBusArrivalsForCurrentTrip...")
                entryPoint.getBusArrivalNotificationService().checkBusArrivalsForCurrentTrip()
                android.util.Log.d("BusArrivalWorker", "Worker completed successfully")
            } else {
                android.util.Log.e("BusArrivalWorker", "Application context is not HiltAndroidApp")
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