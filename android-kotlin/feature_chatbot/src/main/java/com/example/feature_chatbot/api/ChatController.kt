package com.example.feature_chatbot.api
import javax.inject.Inject
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.ChatItem
import com.example.feature_chatbot.data.ChatRequest
import com.example.feature_chatbot.data.Coordinates // <-- Import Coordinates
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Named

class ChatController @Inject constructor(
    @Named("chatbot") private val api: ChatbotApi
) {
    fun sendMessage(
        userInput: String,
        currentLocation: Coordinates?,
        onResult: (ChatItem) -> Unit,
        onError: (ChatItem) -> Unit,
        onNewBotMessage: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val request = ChatRequest(
                    userInput = userInput,
                    currentLocation = currentLocation,
                    currentTimestamp = System.currentTimeMillis()
                )
                val botResponse = api.getResponseFor(request)
                val botItem = ChatItem.BotMessage(UUID.randomUUID().toString(), botResponse)
                onResult(botItem)
                onNewBotMessage(botResponse.toDisplayString())
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)
                val errorItem = ChatItem.BotMessage(UUID.randomUUID().toString(), BotResponse.Error(errorMessage))
                onError(errorItem)
                onNewBotMessage(errorMessage)
                e.printStackTrace()
            }
        }
    }

    private fun BotResponse.toDisplayString(): String = when (this) {
        is BotResponse.Directions -> {
            val routes = suggestedRoutes?.joinToString("\n") {
                "Summary: ${it.summary}\nDuration: ${it.durationInMinutes} minutes"
            } ?: "No routes found."
            "Directions from $startLocation to $endLocation:\n$routes"
        }
        is BotResponse.Message -> message
        is BotResponse.Error -> message
    }

    private fun getErrorMessage(e: Exception): String = when (e) {
        is JsonSyntaxException -> "Sorry, I couldn't understand the response."
        is IOException -> "Connection failed. Please check your network."
        else -> "Unexpected error. Please try again."
    }
}