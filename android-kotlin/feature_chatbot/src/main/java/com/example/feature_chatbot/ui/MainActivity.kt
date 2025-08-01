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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R
import com.example.feature_chatbot.api.ChatbotApi
import com.example.feature_chatbot.api.chatbotApi
import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.data.ChatItem
import com.example.feature_chatbot.domain.ChatController
import com.example.feature_chatbot.domain.SpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private lateinit var scrollToBottomButton: ImageButton

    private var isAutoScrolling = false

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

        chatRecyclerView.post {
            centerGreeting()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUiReferences() {
        scrollToBottomButton = findViewById(R.id.scroll_to_bottom_button)
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatInput = findViewById(R.id.chat_input)
        sendIcon = findViewById(R.id.send_icon)
        micButton = findViewById(R.id.micButton)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(mutableListOf()) {
            scrollToBottom()
        }

        chatRecyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        chatRecyclerView.layoutManager = layoutManager

        chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isAutoScrolling) return

                // delay evaluation one frame to avoid transient layout state flashes
                chatRecyclerView.post {
                    val itemCount = chatAdapter.itemCount
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val atBottom = lastVisible >= itemCount - 1
                    val shouldShow = !atBottom && itemCount > 1

                    if (shouldShow && scrollToBottomButton.visibility != ImageButton.VISIBLE) {
                        scrollToBottomButton.visibility = ImageButton.VISIBLE
                    } else if (!shouldShow && scrollToBottomButton.visibility != ImageButton.GONE) {
                        scrollToBottomButton.visibility = ImageButton.GONE
                    }
                }
            }
        })
    }
    private fun setupChatController() {
        chatController = ChatController(
            adapter = chatAdapter,
            api = chatbotApi,
            onNewBotMessage = { botText ->
                runOnUiThread {
                    animateBotMessageTyping(botText)
                }
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
                        scrollToBottomButton.visibility = ImageButton.GONE
                        chatController.userSent(final)
                    }
                }
            },
            onError = { err ->
                runOnUiThread { chatInput.setText("Error: $err") }
            }
        )
    }

    private fun centerGreeting() {
        val layoutManager = chatRecyclerView.layoutManager as LinearLayoutManager
        val greetingPosition = 0 // Greeting always at position 0

        val viewHolder = chatRecyclerView.findViewHolderForAdapterPosition(greetingPosition)
        if (viewHolder == null) {
            // Try again once layout happens
            chatRecyclerView.post { centerGreeting() }
            return
        }

        val recyclerViewHeight = chatRecyclerView.height
        val greetingHeight = viewHolder.itemView.height

        val topPadding = (recyclerViewHeight / 2) - (greetingHeight / 2)

        // Set padding top dynamically to center the greeting
        chatRecyclerView.setPadding(
            chatRecyclerView.paddingLeft,
            topPadding,
            chatRecyclerView.paddingRight,
            chatRecyclerView.paddingBottom
        )

        // Scroll to position 0 with zero offset to ensure it's visible
        layoutManager.scrollToPositionWithOffset(greetingPosition, 0)
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
        scrollToBottomButton.setOnClickListener {
            scrollToBottom()
        }
    }



    private fun animateBotMessageTyping(botText: String) {
        val botMessage = ChatItem.Message("", isUser = false)
        chatAdapter.addMessage(botMessage)

        val index = chatAdapter.itemCount - 1

        lifecycleScope.launch {
            for (i in botText.indices) {
                val partialText = botText.substring(0, i + 1)
                chatAdapter.updateMessageAt(index, partialText)
                delay(30)
            }
        }
    }

    private fun handleSend() {
        val userMessage = chatInput.text.toString().trim()
        if (userMessage.isNotEmpty() && ::chatController.isInitialized) {
            scrollToBottomButton.visibility = ImageButton.GONE
            chatController.userSent(userMessage)
            chatInput.text.clear()
        }
    }

    private fun scrollToBottom() {
        scrollToBottomButton.visibility = ImageButton.GONE

        if (chatAdapter.itemCount > 0) {
            chatRecyclerView.post {
                isAutoScrolling = true
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                chatRecyclerView.postDelayed({ isAutoScrolling = false }, 50)
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