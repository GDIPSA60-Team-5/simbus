package com.example.busappkotlin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.busappkotlin.R
import com.example.feature_chatbot.ui.ChatbotActivity
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val secureStorage = SecureStorageManager(this)
        val token = secureStorage.getToken()

        val intent = if (token != null) {
            Intent(this, HomeActivity::class.java)  // Token exists: go Home
        } else {
            Intent(this, LoginActivity::class.java) // No token: go Login
        }

    private fun setupUI() {
        val intent = Intent(this, NavigateActivity::class.java)
        startActivity(intent)
        startActivity(intent)
        finish() // Close MainActivity so user can't go back to it
    }
}