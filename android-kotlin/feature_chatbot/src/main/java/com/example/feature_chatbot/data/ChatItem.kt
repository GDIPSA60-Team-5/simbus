package com.example.feature_chatbot.data

sealed class ChatItem {
    object Greeting : ChatItem()
    data class UserMessage(val text: String) : ChatItem()
    data class BotMessage(val botResponse: BotResponse) : ChatItem()
    data class TypingIndicator(val message: String = "Typing...") : ChatItem()
}