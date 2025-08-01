package com.example.feature_chatbot.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R
import com.example.feature_chatbot.api.DummyDirectionsApi
import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.domain.ChatController
import com.example.feature_chatbot.domain.SpeechManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1
    }

    private lateinit var speechManager: SpeechManager
    private lateinit var chatController: ChatController

    private lateinit var micButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatInput: EditText
    private lateinit var sendIcon: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        applyWindowInsets()
        initUiReferences()
        setupRecyclerView()
        setupChatController()
        setupSpeechManager()
        setupListeners()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUiReferences() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatInput = findViewById(R.id.chat_input)
        sendIcon = findViewById(R.id.send_icon)
        micButton = findViewById(R.id.micButton)
    }

    private fun setupRecyclerView() {
        // Pass callback to adapter for auto-scrolling
        chatAdapter = ChatAdapter(mutableListOf()) {
            scrollToBottom()
        }

        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
    }

    private fun setupChatController() {
        val directionsApi = DummyDirectionsApi()
        chatController = ChatController(
            adapter = chatAdapter,
            api = directionsApi,
            onNewBotMessage = { botText ->
                // This callback is already handled by the adapter's onMessageAdded callback
                // No need to duplicate scrolling logic here
            }
        )
    }

    private fun setupSpeechManager() {
        speechManager = SpeechManager(
            context = this,
            onPartial = { partial ->
                runOnUiThread { chatInput.setText("Listening: $partial") }
            },
            onFinal = { final ->
                runOnUiThread {
                    chatInput.setText(final)
                    if (::chatController.isInitialized) {
                        chatController.userSent(final)
                        // Scrolling is now handled by the adapter callback
                    }
                }
            },
            onError = { err ->
                runOnUiThread { chatInput.setText("Error: $err") }
            }
        )
    }

    private fun setupListeners() {
        sendIcon.setOnClickListener { handleSend() }
        micButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                speechManager.startListening()
            } else {
                ensureAudioPermission()
            }
        }
    }

    private fun handleSend() {
        val userMessage = chatInput.text.toString().trim()
        if (userMessage.isNotEmpty() && ::chatController.isInitialized) {
            chatController.userSent(userMessage)
            chatInput.text.clear()
            // Scrolling is now handled by the adapter callback
        }
    }

    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            // Use post to ensure the layout has been updated
            chatRecyclerView.post {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    private fun ensureAudioPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechManager.startListening()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required to use voice input.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
    }
}