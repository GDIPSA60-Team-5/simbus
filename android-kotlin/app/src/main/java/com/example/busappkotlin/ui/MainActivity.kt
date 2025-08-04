package com.example.busappkotlin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.busappkotlin.R
import com.example.feature_chatbot.ui.ChatbotActivity
import iss.nus.edu.sg.appfiles.feature_login.LoginActivity
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager

class MainActivity : AppCompatActivity() {

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            setupUI()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val secureStorage = SecureStorageManager(this)
        val token = secureStorage.getToken()

        // Force login screen for testing
        val intent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(intent)

        // Original check, now commented for testing
        /*
        val secureStorage = SecureStorageManager(this)
        val token = secureStorage.getToken()

        if (token.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            loginLauncher.launch(intent)
        } else {
            setupUI()
        }
        */
    }

    private fun setupUI() {
        val chatboxButton: Button = findViewById(R.id.chatboxButton)
        chatboxButton.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }
    }
}
