package com.example.feature_chatbot.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
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
import com.example.feature_chatbot.api.ApiClient
import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.domain.ChatController
import com.example.feature_chatbot.domain.SpeechManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1
        private const val SCROLL_VISIBILITY_THRESHOLD = 2
        private const val SCROLL_ANIMATION_DELAY = 300L
    }

    // Managers
    private lateinit var speechManager: SpeechManager
    private lateinit var chatController: ChatController

    // UI Components
    private lateinit var micButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatInput: EditText
    private lateinit var sendIcon: ImageButton
    private lateinit var scrollToBottomButton: ImageButton

    // State
    private var isAutoScrolling = false
    private var isGreetingPaddingRemoved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        initializeComponents()
        setupListeners()

        // Center greeting after layout is complete
        chatRecyclerView.post { centerGreeting() }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        applyWindowInsets()
        initUIReferences()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUIReferences() {
        scrollToBottomButton = findViewById(R.id.scroll_to_bottom_button)
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatInput = findViewById(R.id.chat_input)
        sendIcon = findViewById(R.id.send_icon)
        micButton = findViewById(R.id.micButton)
    }

    private fun initializeComponents() {
        setupRecyclerView()
        setupChatController()
        setupSpeechManager()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { scrollToBottom() }

        chatAdapter.replaceAll(emptyList())

        with(chatRecyclerView) {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addOnScrollListener(createScrollListener())
        }
    }

    private fun createScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isAutoScrolling) return

                updateScrollToBottomButtonVisibility()
            }
        }
    }
    private fun updateScrollToBottomButtonVisibility() {
        chatRecyclerView.post {
            val itemCount = chatAdapter.itemCount
            val canScrollFurther = chatRecyclerView.canScrollVertically(1) // true if not at bottom
            val shouldShow = canScrollFurther && itemCount > 1
            scrollToBottomButton.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }
    }

    private fun setupChatController() {
        chatController = ChatController(
            adapter = chatAdapter,
            api = ApiClient.chatbotApi,
            onNewBotMessage = { botText ->
                // Optional: Add any external notifications here
                // For example: showBotMessageNotification(botText)
            }
        )
    }

    private fun setupSpeechManager() {
        speechManager = SpeechManager(
            context = this,
            onPartial = { partial ->
                runOnUiThread {
                    chatInput.setText(getString(R.string.listening_partial, partial))
                }
            },
            onFinal = { final ->
                runOnUiThread {
                    chatInput.setText(final)
                    handleUserMessage(final)
                }
            },
            onError = { error ->
                runOnUiThread {
                    chatInput.setText(getString(R.string.speech_error, error))
                }
            }
        )
    }

    private fun setupListeners() {
        sendIcon.setOnClickListener { handleSendClick() }
        micButton.setOnClickListener { handleMicClick() }
        scrollToBottomButton.setOnClickListener { scrollToBottom() }
    }

    private fun handleSendClick() {
        val userMessage = chatInput.text.toString().trim()
        if (userMessage.isNotEmpty()) {
            handleUserMessage(userMessage)
            chatInput.text.clear()
        }
    }

    private fun handleMicClick() {
        if (hasAudioPermission()) {
            speechManager.startListening()
        } else {
            requestAudioPermission()
        }
    }

    private fun handleUserMessage(message: String) {
        if (!::chatController.isInitialized) return

        removeGreetingPaddingIfNeeded()
        scrollToBottomButton.visibility = ImageButton.GONE
        chatController.userSent(message)
    }

    private fun removeGreetingPaddingIfNeeded() {
        if (!isGreetingPaddingRemoved) {
            chatRecyclerView.setPadding(
                chatRecyclerView.paddingLeft,
                0,
                chatRecyclerView.paddingRight,
                chatRecyclerView.paddingBottom
            )
            isGreetingPaddingRemoved = true
        }
    }


    private fun hasAudioPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO
        )
    }

    private fun centerGreeting() {
        val layoutManager = chatRecyclerView.layoutManager as LinearLayoutManager
        val greetingPosition = 0

        chatRecyclerView.post {
            val viewHolder = chatRecyclerView.findViewHolderForAdapterPosition(greetingPosition)

            if (viewHolder == null) {
                // ViewHolder not ready, retry after next layout pass
                chatRecyclerView.post { centerGreeting() }
                return@post
            }

            applyGreetingCentering(viewHolder, layoutManager, greetingPosition)
        }
    }

    private fun applyGreetingCentering(
        viewHolder: RecyclerView.ViewHolder,
        layoutManager: LinearLayoutManager,
        greetingPosition: Int
    ) {
        val recyclerViewHeight = chatRecyclerView.height
        val greetingHeight = viewHolder.itemView.height
        val topPadding = (recyclerViewHeight / 2) - (greetingHeight / 2)

        chatRecyclerView.setPadding(
            chatRecyclerView.paddingLeft,
            topPadding,
            chatRecyclerView.paddingRight,
            chatRecyclerView.paddingBottom
        )

        layoutManager.scrollToPositionWithOffset(greetingPosition, 0)
    }

    private fun scrollToBottom() {
        scrollToBottomButton.visibility = ImageButton.GONE

        if (chatAdapter.itemCount > 0) {
            chatRecyclerView.post {
                isAutoScrolling = true
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                chatRecyclerView.postDelayed(
                    { isAutoScrolling = false },
                    SCROLL_ANIMATION_DELAY
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speechManager.startListening()
                } else {
                    showPermissionDeniedMessage()
                }
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            getString(R.string.microphone_permission_required),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
    }
}