package com.example.feature_chatbot.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.core.api.UserApi
import com.example.feature_chatbot.R
import com.example.feature_chatbot.databinding.ActivityChatbotBinding
import com.example.feature_chatbot.data.ChatAdapter
import com.example.core.model.Coordinates
import com.example.feature_chatbot.api.ChatController
import com.example.feature_chatbot.data.ChatItem
import com.example.feature_chatbot.domain.SpeechManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ChatbotActivity : AppCompatActivity() {

    companion object {
        private const val SCROLL_ANIMATION_DELAY = 300L
    }

    private lateinit var binding: ActivityChatbotBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Coordinates? = null

    // Permission launchers
    private fun locationPermissionLauncherWithCallback(onComplete: () -> Unit) =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to use this feature.",
                    Toast.LENGTH_LONG
                ).show()
            }
            onComplete()
        }

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                speechManager.startListening()
            } else {
                showPermissionDeniedMessage()
            }
        }

    // Managers
    private lateinit var speechManager: SpeechManager
    @Inject
    lateinit var chatController: ChatController
    @Inject
    lateinit var userApi: UserApi

    // UI Components
    private lateinit var chatAdapter: ChatAdapter

    // State
    private var isAutoScrolling = false
    private var isGreetingPaddingRemoved = false
    private var username: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        initializeComponents()
        setupListeners()
        loadUserData()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission {
        binding.chatRecyclerView.post {
                centerGreeting {
                    checkExternalIntents()
                }
            }
        }
    }

    private fun checkExternalIntents() {
        intent?.getStringExtra("userMessage")?.takeIf { it.isNotBlank() }?.let { message ->
            handleUserMessage(message)
        }
    }

    private fun checkLocationPermission(onComplete: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
                onComplete()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain if needed, then request
                locationPermissionLauncherWithCallback(onComplete)
                    .launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                locationPermissionLauncherWithCallback(onComplete)
                    .launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                location?.let {
                    this.currentLocation = Coordinates(it.latitude, it.longitude)
                    Toast.makeText(this, "Location is ready!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        applyWindowInsets()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeComponents() {
        setupRecyclerView()
        setupSpeechManager()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { scrollToBottom() }
        chatAdapter.replaceAll(emptyList())

        with(binding.chatRecyclerView) {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatbotActivity)
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
        binding.chatRecyclerView.post {
            val itemCount = chatAdapter.itemCount
            val canScrollFurther = binding.chatRecyclerView.canScrollVertically(1)
            val shouldShow = canScrollFurther && itemCount > 1
            binding.scrollToBottomButton.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }
    }

    private fun setupSpeechManager() {
        speechManager = SpeechManager(
            context = this,
            onPartial = { partial ->
                runOnUiThread {
                    binding.chatInput.setText(getString(R.string.listening_partial, partial))
                }
            },
            onFinal = { final ->
                runOnUiThread {
                    binding.chatInput.setText(final)
                    handleUserMessage(final)
                }
            },
            onError = { error ->
                runOnUiThread {
                    binding.chatInput.setText(getString(R.string.speech_error, error))
                }
            }
        )
    }

    private fun setupListeners() {
        binding.sendIcon.setOnClickListener { handleSendClick() }
        binding.micButton.setOnClickListener { handleMicClick() }
        binding.scrollToBottomButton.setOnClickListener { scrollToBottom() }
        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun handleSendClick() {
        val userMessage = binding.chatInput.text.toString().trim()
        if (userMessage.isNotEmpty()) {
            handleUserMessage(userMessage)
            binding.chatInput.text.clear()
        }
    }

    private fun handleMicClick() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            speechManager.startListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun handleUserMessage(message: String) {
        if (message.isEmpty()) return

        removeGreetingPaddingIfNeeded()
        binding.scrollToBottomButton.visibility = View.GONE
        val userChatItem = ChatItem.UserMessage(UUID.randomUUID().toString(), message)
        val typingIndicator = ChatItem.TypingIndicator()
        chatAdapter.addChatItem(userChatItem)
        chatAdapter.addChatItem(typingIndicator)

        chatController.sendMessage(
            userInput = message,
            currentLocation = currentLocation,
            onResult = { botMessage ->
                chatAdapter.replaceLastChatItem(botMessage)
            },
            onError = { errorMessage ->
                chatAdapter.replaceLastChatItem(errorMessage)
            },
            onNewBotMessage = {
                // Optional: show toast, log, or do nothing
            }
        )
    }

    private fun removeGreetingPaddingIfNeeded() {
        if (!isGreetingPaddingRemoved) {
            binding.chatRecyclerView.setPadding(
                binding.chatRecyclerView.paddingLeft,
                0,
                binding.chatRecyclerView.paddingRight,
                binding.chatRecyclerView.paddingBottom
            )
            isGreetingPaddingRemoved = true
        }
    }

    private fun centerGreeting(onComplete: (() -> Unit)? = null) {
        val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
        val greetingPosition = 0

        binding.chatRecyclerView.post {
            val viewHolder = binding.chatRecyclerView.findViewHolderForAdapterPosition(greetingPosition)

            if (viewHolder == null) {
                binding.chatRecyclerView.post { centerGreeting(onComplete) }
                return@post
            }

            applyGreetingCentering(viewHolder, layoutManager, greetingPosition)
            onComplete?.invoke()
        }
    }


    private fun applyGreetingCentering(
        viewHolder: RecyclerView.ViewHolder,
        layoutManager: LinearLayoutManager,
        greetingPosition: Int
    ) {
        val recyclerViewHeight = binding.chatRecyclerView.height
        val greetingHeight = viewHolder.itemView.height
        val topPadding = (recyclerViewHeight / 2) - (greetingHeight / 2)

        binding.chatRecyclerView.setPadding(
            binding.chatRecyclerView.paddingLeft,
            topPadding,
            binding.chatRecyclerView.paddingRight,
            binding.chatRecyclerView.paddingBottom
        )

        layoutManager.scrollToPositionWithOffset(greetingPosition, 0)
    }

    private fun scrollToBottom() {
        binding.scrollToBottomButton.visibility = View.GONE

        if (chatAdapter.itemCount > 0) {
            binding.chatRecyclerView.post {
                isAutoScrolling = true
                binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                binding.chatRecyclerView.postDelayed(
                    { isAutoScrolling = false },
                    SCROLL_ANIMATION_DELAY
                )
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

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val response = userApi.getCurrentUser()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        username = user.username
                        updateGreeting()
                    }
                }
            } catch (e: Exception) {
                // Keep default username if API call fails
            }
        }
    }

    private fun updateGreeting() {
        // Update username in adapter and refresh the greeting
        if (::chatAdapter.isInitialized) {
            chatAdapter.username = username
            chatAdapter.notifyItemChanged(0) // Greeting is always at position 0
        }
    }

    fun getUsername(): String = username

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
    }
}