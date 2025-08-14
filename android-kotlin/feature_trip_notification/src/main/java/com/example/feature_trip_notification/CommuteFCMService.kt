package com.example.feature_trip_notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommuteFCMService : FirebaseMessagingService() {
    
    @Inject
    lateinit var tripNotificationManager: TripNotificationManager
    
    companion object {
        private const val TAG = "CommuteFCMService"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")
        
        // Handle data payload
        val data = remoteMessage.data
        val type = data["type"]
        
        when (type) {
            "COMMUTE_STARTED" -> handleCommuteStarted(data)
            else -> {
                // Handle general notification
                remoteMessage.notification?.let { notification ->
                    tripNotificationManager.showInstructionNotification(
                        "${notification.title}: ${notification.body}"
                    )
                }
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        // TODO: Send token to backend via user API
    }
    
    private fun handleCommuteStarted(data: Map<String, String>) {
        val commutePlanName = data["commutePlanName"] ?: "Your Commute"
        
        Log.d(TAG, "Handling commute started: $commutePlanName")
        
        tripNotificationManager.showTripStartNotification("Start", "Destination")
    }
}