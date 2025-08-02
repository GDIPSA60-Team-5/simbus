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
import java.util.UUID

class ChatController(
    private val adapter: ChatAdapter,
    private val api: ChatbotApi,
    private val onNewBotMessage: (String) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun userSent(text: String) {
        // Add the user's message
        adapter.addChatItem(ChatItem.UserMessage(UUID.randomUUID().toString(), text))

        // Add temporary typing indicator
        adapter.addChatItem(ChatItem.TypingIndicator())

        scope.launch {
            try {
                val botResponse = api.getResponseFor(text)

                adapter.replaceLastChatItem(
                    ChatItem.BotMessage(UUID.randomUUID().toString(), botResponse)
                )
                onNewBotMessage(botResponse.toDisplayString())

            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)

                // Replace typing with error
                adapter.replaceLastChatItem(
                    ChatItem.BotMessage(UUID.randomUUID().toString(), BotResponse.Error(errorMessage))
                )
                onNewBotMessage(errorMessage)

                e.printStackTrace()
            }
        }
    }

    private fun BotResponse.toDisplayString(): String {
        return when (this) {
            is BotResponse.Directions -> {
                val routesText = suggestedRoutes?.joinToString("\n") { route ->
                    "Summary: ${route.summary}\nDuration: ${route.durationInMinutes} minutes"
                } ?: "No routes found."
                "Directions from $startLocation to $endLocation:\n$routesText"
            }
            is BotResponse.Message -> text
            is BotResponse.Error -> message
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
