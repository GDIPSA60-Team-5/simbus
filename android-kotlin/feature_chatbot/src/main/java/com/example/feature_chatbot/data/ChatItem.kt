package com.example.feature_chatbot.data

sealed class ChatItem {
    object Greeting : ChatItem()
    data class Message(val text: String, val isUser: Boolean) : ChatItem()
}
