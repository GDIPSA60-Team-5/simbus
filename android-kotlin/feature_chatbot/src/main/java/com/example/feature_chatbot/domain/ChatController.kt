package com.example.feature_chatbot.domain

import com.example.feature_chatbot.api.ChatbotApi
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.data.ChatItem
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class ChatController(
    private val adapter: ChatAdapter,
    private val api: ChatbotApi,
    private val onNewBotMessage: (String) -> Unit // This callback can still be used for external logging/toasts
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun userSent(text: String) {
        // Add the user's message
        adapter.addChatItem(ChatItem.UserMessage(text))

        // Add a temporary "Typing..." message
        val typingIndicator = ChatItem.TypingIndicator()
        adapter.addChatItem(typingIndicator)

        scope.launch {
            try {
                val botResponse = api.getResponseFor(text)

                // Replace the "Typing..." message with the actual bot response
                adapter.replaceLastChatItem(ChatItem.BotMessage(botResponse))

                onNewBotMessage(botResponse.toDisplayString())

            } catch (e: Exception) {
                // Replace the "Typing..." message with an error message
                val userFacingMessage = getErrorMessage(e)
                adapter.replaceLastChatItem(ChatItem.BotMessage(BotResponse.Error(userFacingMessage)))
                onNewBotMessage(userFacingMessage) // Still notify if needed
                e.printStackTrace() // Log the full error for debugging purposes
            }
        }
    }

    // Used for logging
    private fun BotResponse.toDisplayString(): String {
        return when (this) {
            is BotResponse.Directions -> {
                val routesText = suggestedRoutes?.joinToString(separator = "\n") { route ->
                    "Summary: ${route.summary}\nDuration: ${route.durationInMinutes} minutes"
                } ?: "No routes found."
                "Directions from $startLocation to $endLocation:\n$routesText"
            }
            is BotResponse.Message -> this.text
            is BotResponse.Error -> this.message
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is JsonSyntaxException -> "Sorry, I had trouble understanding the data from the server. Please try again."
            is IOException -> "Sorry, I couldn't connect to the server. Please check your internet connection."
            else -> "An unexpected error occurred. Please try again later."
        }
    }
}