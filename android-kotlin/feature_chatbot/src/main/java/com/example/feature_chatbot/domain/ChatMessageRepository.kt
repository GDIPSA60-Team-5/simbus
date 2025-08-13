package com.example.feature_chatbot.domain

import com.example.feature_chatbot.data.ChatItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMessageRepository @Inject constructor() {
    
    // In-memory storage that persists only during app lifecycle
    private val chatMessages = mutableListOf<ChatItem>()
    
    init {
        // Always start with greeting
        chatMessages.add(ChatItem.Greeting)
    }
    
    fun getMessages(): List<ChatItem> = chatMessages.toList()
    
    fun addMessage(message: ChatItem) {
        chatMessages.add(message)
    }
    
    fun replaceLastMessage(newMessage: ChatItem) {
        if (chatMessages.isNotEmpty()) {
            chatMessages[chatMessages.lastIndex] = newMessage
        } else {
            chatMessages.add(newMessage)
        }
    }
    
    fun getMessageCount(): Int = chatMessages.size
    
    fun clearMessages() {
        chatMessages.clear()
        chatMessages.add(ChatItem.Greeting)
    }
}