package com.example.feature_chatbot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.feature_chatbot.data.ChatItem
import com.example.feature_chatbot.domain.ChatMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatMessageRepository
) : ViewModel() {
    
    private val _chatMessages = MutableLiveData<List<ChatItem>>()
    val chatMessages: LiveData<List<ChatItem>> = _chatMessages
    
    init {
        _chatMessages.value = repository.getMessages()
    }
    
    fun addMessage(message: ChatItem) {
        repository.addMessage(message)
        _chatMessages.value = repository.getMessages()
    }
    
    fun replaceLastMessage(newMessage: ChatItem) {
        repository.replaceLastMessage(newMessage)
        _chatMessages.value = repository.getMessages()
    }
    
    fun getMessageCount(): Int = repository.getMessageCount()
    
    fun clearMessages() {
        repository.clearMessages()
        _chatMessages.value = repository.getMessages()
    }
}