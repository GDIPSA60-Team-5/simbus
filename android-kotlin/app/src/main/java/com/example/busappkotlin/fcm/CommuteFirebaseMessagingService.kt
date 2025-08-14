package com.example.busappkotlin.fcm

import android.content.Intent
import android.util.Log
import com.example.core.api.UpdateFcmTokenRequest
import com.example.core.api.UserApi
import com.example.core.di.SecureStorageManager
import com.example.feature_trip_notification.TripNotificationManager
import com.example.feature_guidemap.MapsNavigationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CommuteFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tripNotificationManager: TripNotificationManager
    
    @Inject
    lateinit var userApi: UserApi
    
    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    companion object {
        private const val TAG = "CommuteFCM"
        private const val TYPE_COMMUTE_START = "commute_start"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            handleNotification(it.title, it.body)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"]
        
        when (messageType) {
            TYPE_COMMUTE_START -> {
                val commutePlanId = data["commute_plan_id"]
                val startLocation = data["start_location"] ?: "Unknown"
                val endLocation = data["end_location"] ?: "Unknown"
                
                Log.d(TAG, "Handling commute start for plan: $commutePlanId")
                
                // Show "Commute Starting" notification
                tripNotificationManager.showTripStartNotification(startLocation, endLocation)
                
                // Start MapsNavigationActivity with the commute plan
                startCommuteNavigation(commutePlanId, startLocation, endLocation)
            }
            else -> {
                Log.d(TAG, "Unknown message type: $messageType")
            }
        }
    }

    private fun handleNotification(title: String?, body: String?) {
        // Handle regular notification display if needed
        Log.d(TAG, "Received notification - Title: $title, Body: $body")
    }

    private fun startCommuteNavigation(commutePlanId: String?, startLocation: String, endLocation: String) {
        // Create intent to start MapsNavigationActivity
        val intent = Intent(this, MapsNavigationActivity::class.java).apply {
            putExtra("commute_plan_id", commutePlanId)
            putExtra("start_location", startLocation)
            putExtra("end_location", endLocation)
            putExtra("trigger_source", "scheduled_commute")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        try {
            startActivity(intent)
            Log.d(TAG, "Started MapsNavigationActivity for commute")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MapsNavigationActivity", e)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to server
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Sending token to server: $token")
        
        // Check if user is logged in
        if (secureStorageManager.getToken().isNullOrEmpty()) {
            Log.d(TAG, "User not logged in, skipping token update")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userApi.updateFcmToken(UpdateFcmTokenRequest(token))
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully updated FCM token on server")
                } else {
                    Log.e(TAG, "Failed to update FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token", e)
            }
        }
    }
}