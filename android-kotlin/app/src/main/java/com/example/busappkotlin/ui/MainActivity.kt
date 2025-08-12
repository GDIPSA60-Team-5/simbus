package com.example.busappkotlin.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import com.example.core.di.SecureStorageManager
import iss.nus.edu.sg.appfiles.feature_navigatebar.NavigateActivity
import iss.nus.edu.sg.appfiles.feature_notification.PushNotificationService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // This is the FCM token for receiving push notification
        PushNotificationService().retryPendingToken(this)

        val secureStorage = SecureStorageManager(this)
        val token = secureStorage.getToken()

        Log.d("MainActivity", "Token retrieved: $token")

        val intent = if (!token.isNullOrEmpty()) {
            Intent(this, NavigateActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start activity", e)
        }
        finish()
    }
}
