package com.example.feature_guidemap

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.core.service.TripService
import com.example.feature_trip_notification.NotificationPollingService
import com.example.feature_trip_notification.TripNotificationManager
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class TripForegroundService : Service() {
    
    @Inject
    lateinit var tripService: TripService
    
    @Inject
    lateinit var tripNotificationManager: TripNotificationManager
    
    @Inject
    lateinit var notificationPollingService: NotificationPollingService
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        const val ACTION_START_TRIP_TRACKING = "START_TRIP_TRACKING"
        const val ACTION_STOP_TRIP_TRACKING = "STOP_TRIP_TRACKING"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "trip_service_channel"
        
        fun startTripTracking(context: Context) {
            val intent = Intent(context, TripForegroundService::class.java).apply {
                action = ACTION_START_TRIP_TRACKING
            }
            context.startForegroundService(intent)
        }
        
        fun stopTripTracking(context: Context) {
            val intent = Intent(context, TripForegroundService::class.java).apply {
                action = ACTION_STOP_TRIP_TRACKING
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }
    
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRIP_TRACKING -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationTracking()
                notificationPollingService.startPolling()
            }
            ACTION_STOP_TRIP_TRACKING -> {
                stopLocationTracking()
                notificationPollingService.stopPolling()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        serviceScope.cancel()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Trip Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Service for tracking active trips"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MapsNavigationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trip Active")
            .setContentText("Tracking your journey")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationTracking() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    checkTripProgress(location)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
    
    private fun stopLocationTracking() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }
    
    private fun checkTripProgress(location: Location) {
        serviceScope.launch {
            try {
                // Update location on backend for proximity checks
                updateLocationOnBackend(location.latitude, location.longitude)
                
                // Check if we should advance to next leg
                // This is a simplified example - in reality you'd check if user 
                // is close to the next waypoint
                checkAndAdvanceTrip()
                
            } catch (e: Exception) {
                android.util.Log.e("TripForegroundService", "Error checking trip progress", e)
            }
        }
    }
    
    private suspend fun updateLocationOnBackend(latitude: Double, longitude: Double) {
        try {
            // This would call your backend API to update location
            // For now, just log the location
            android.util.Log.d("TripForegroundService", "Location: $latitude, $longitude")
        } catch (e: Exception) {
            android.util.Log.e("TripForegroundService", "Failed to update location", e)
        }
    }
    
    private suspend fun checkAndAdvanceTrip() {
        try {
            // Get current trip status from TripService
            // Check if user has reached a waypoint
            // If so, call backend to advance trip
            
            // This is where you'd implement the logic to automatically
            // advance the trip based on location proximity
            android.util.Log.d("TripForegroundService", "Checking trip advancement conditions")
            
        } catch (e: Exception) {
            android.util.Log.e("TripForegroundService", "Failed to check trip advancement", e)
        }
    }
}