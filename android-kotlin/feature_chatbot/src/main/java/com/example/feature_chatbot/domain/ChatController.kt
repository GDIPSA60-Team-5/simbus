package com.example.feature_chatbot.domain

import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.data.ChatMessage
import com.example.feature_chatbot.api.DirectionsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatController(
    private val adapter: ChatAdapter,
    private val api: DirectionsApi,
    private val onNewBotMessage: (String) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun userSent(text: String) {
        adapter.addMessage(ChatMessage(text, isUser = true))

        // Handle bot response
        scope.launch {
            try {
                val botResponse = api.getResponseFor(text)
                adapter.addMessage(ChatMessage(botResponse, isUser = false))
                onNewBotMessage(botResponse)
            } catch (e: Exception) {
                adapter.addMessage(ChatMessage("Sorry, I couldn't process that.", isUser = false))
            }
        }
    }
}