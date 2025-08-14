package com.example.busappkotlin.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.core.api.UpdateFcmTokenRequest
import com.example.core.api.UserApi

import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import com.example.core.di.SecureStorageManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import iss.nus.edu.sg.appfiles.feature_navigatebar.NavigateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userApi: UserApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val secureStorage = SecureStorageManager(this)
        val token = secureStorage.getToken()

        Log.d("MainActivity", "Token retrieved: $token")

        if (!token.isNullOrEmpty()) {
            FirebaseApp.initializeApp(this)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Log.d("FCM", "Current FCM token: $fcmToken")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = userApi.updateFcmToken(UpdateFcmTokenRequest(fcmToken))
                            if (response.isSuccessful) {
                                Log.d("FCM", "Token updated in user table after login")
                            } else {
                                Log.e("FCM", "Failed to update token after login: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("FCM", "Exception sending token after login", e)
                        }
                    }
                } else {
                    Log.e("FCM", "Failed to get FCM token", task.exception)
                }
            }
        }

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
