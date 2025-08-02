package com.example.feature_chatbot.data

sealed class ChatItem {
    object Greeting : ChatItem()
    data class UserMessage(val id: String, val text: String) : ChatItem()
    data class BotMessage(val id: String, val botResponse: BotResponse) : ChatItem()
    data class TypingIndicator(val message: String = "Typing...") : ChatItem()
}
