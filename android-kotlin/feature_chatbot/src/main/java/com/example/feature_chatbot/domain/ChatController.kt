// File: ChatController.kt
package com.example.feature_chatbot.domain

import com.example.feature_chatbot.api.ChatbotApi
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.data.ChatItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatController(
    private val adapter: ChatAdapter,
    private val api: ChatbotApi,
    private val onNewBotMessage: (String) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun userSent(text: String) {
        adapter.addMessage(ChatItem.Message(text, isUser = true))

        // This assumes the bot response will be a String
        val temporaryBotMessage = ChatItem.Message("Typing...", isUser = false)
        adapter.addMessage(temporaryBotMessage)

        scope.launch {
            try {
                // The API now returns the sealed class
                val botResponse = api.getResponseFor(text)

                // Remove the "Typing..." message
                removeTyping(temporaryBotMessage)

                // Use a 'when' expression to handle each type of response
                when (botResponse) {
                    is BotResponse.Directions -> {
                        // Format the DirectionsResponse into a displayable string
                        val message = formatDirectionsResponse(botResponse)
                        onNewBotMessage(message)
                    }
                    is BotResponse.Message -> {
                        // Display the simple message
                        onNewBotMessage(botResponse.text)
                    }
                    is BotResponse.Error -> {
                        // Display the error message
                        onNewBotMessage(botResponse.message)
                    }
                }
            } catch (e: Exception) {
                // This catch block handles network errors or JSON parsing issues
                removeTyping(temporaryBotMessage)
                onNewBotMessage("Sorry, I couldn't connect to the service.")
                e.printStackTrace()
            }
        }
    }
    private fun formatDirectionsResponse(response: BotResponse.Directions): String {
        val routesText = response.suggestedRoutes?.joinToString(separator = "\n") { route ->
            // Use the actual properties from the Route data class
            val durationText = "${route.durationInMinutes} minutes"

            "Summary: ${route.summary}\nDuration: $durationText"
        } ?: "No routes found." // Handle the case where suggestedRoutes is null or empty

        return "Directions from ${response.startLocation} to ${response.endLocation}:\n$routesText"
    }
    // The other functions can remain the same
    private fun removeTyping(msg: ChatItem.Message) {
        adapter.removeMessage(msg)
    }

    private fun addBotMessage(text: String) {
        onNewBotMessage(text)
    }
}