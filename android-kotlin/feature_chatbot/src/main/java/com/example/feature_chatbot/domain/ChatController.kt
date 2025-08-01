package com.example.feature_chatbot.domain

import com.example.feature_chatbot.data.ChatAdapter
import com.example.feature_chatbot.data.ChatItem
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
        adapter.addMessage(ChatItem.Message(text, isUser = true))

        scope.launch {
            try {
                val botResponse = api.getResponseFor(text)
                addBotMessage(botResponse)
            } catch (e: Exception) {
                addBotMessage("Sorry, I couldn't process that.")
                e.printStackTrace()
            }
        }
    }


    private fun addBotMessage(text: String) {
        onNewBotMessage(text)
    }

    private fun removeTyping(msg: ChatItem.Message) {
         adapter.removeMessage(msg)
    }
}
