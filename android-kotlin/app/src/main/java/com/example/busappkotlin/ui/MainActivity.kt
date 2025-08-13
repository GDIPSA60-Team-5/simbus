package com.example.busappkotlin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.NavigateActivity
import iss.nus.edu.sg.appfiles.feature_notification.PushNotificationService
import iss.nus.edu.sg.appfiles.feature_notification.api.DeviceTokenController
import com.google.firebase.messaging.FirebaseMessaging
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var deviceTokenController: DeviceTokenController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val secureStorage = SecureStorageManager(this)
        val authToken = secureStorage.getToken()
        Log.d("MainActivity", "Auth token retrieved: $authToken")

        if (!authToken.isNullOrEmpty()) {
            // Only send FCM token if user is logged in (authToken exists)

            // Log pending FCM token
            PushNotificationService.logSavedToken(this)

            // Retry sending pending FCM token
            PushNotificationService.retryPendingToken(this, deviceTokenController)

            // Fetch current FCM token and send to backend
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Log.d("FCM", "Current FCM token: $fcmToken")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val deviceId = DeviceIdUtil.getDeviceId(this@MainActivity)
                            val result = deviceTokenController.updateDeviceToken(deviceId, fcmToken)
                            result.onSuccess { Log.d("FCM", "Token sent: ${it.message}") }
                            result.onFailure { Log.e("FCM", "Failed to send token", it) }
                        } catch (e: Exception) {
                            Log.e("FCM", "Exception sending token", e)
                        }
                    }
                } else {
                    Log.e("FCM", "Failed to get FCM token", task.exception)
                }
            }
        }

        val intent = if (!authToken.isNullOrEmpty()) {
            Intent(this, NavigateActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
