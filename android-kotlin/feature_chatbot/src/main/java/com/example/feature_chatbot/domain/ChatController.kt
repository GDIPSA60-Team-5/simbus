// File: ChatController.kt
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
    private val onNewBotMessage: (String) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun userSent(text: String) {
        // Add the user's message and a temporary "Typing..." message
        adapter.addMessage(ChatItem.Message(text, isUser = true))
        val temporaryBotMessage = ChatItem.Message("Typing...", isUser = false)
        adapter.addMessage(temporaryBotMessage)

        scope.launch {
            try {
                val botResponse = api.getResponseFor(text)

                // Remove the "Typing..." message before displaying the new one
                removeTyping(temporaryBotMessage)

                // Get the displayable message from the bot response
                val messageToDisplay = botResponse.toDisplayString()
                onNewBotMessage(messageToDisplay)

            } catch (e: Exception) {
                removeTyping(temporaryBotMessage)

                // Log the full error for debugging purposes
                e.printStackTrace()

                val userFacingMessage = getErrorMessage(e)
                onNewBotMessage(userFacingMessage)
            }
        }
    }

    // Moved the formatting logic into the sealed class itself
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

    // Extracted the error message handling into a separate function
    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is JsonSyntaxException -> "Sorry, I had trouble understanding the data from the server. Please try again."
            is IOException -> "Sorry, I couldn't connect to the server. Please check your internet connection."
            else -> "An unexpected error occurred. Please try again later."
        }
    }

    private fun removeTyping(msg: ChatItem.Message) {
        adapter.removeMessage(msg)
    }
}