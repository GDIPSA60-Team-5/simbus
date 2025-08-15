package com.example.feature_chatbot.util

import androidx.recyclerview.widget.DiffUtil
import com.example.feature_chatbot.data.ChatItem

class ChatItemDiffCallback : DiffUtil.ItemCallback<ChatItem>() {

    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return when {
            oldItem is ChatItem.Greeting && newItem is ChatItem.Greeting -> true
            oldItem is ChatItem.UserMessage && newItem is ChatItem.UserMessage -> oldItem.id == newItem.id
            oldItem is ChatItem.BotMessage && newItem is ChatItem.BotMessage -> oldItem.id == newItem.id
            oldItem is ChatItem.TypingIndicator && newItem is ChatItem.TypingIndicator -> true
            else -> false
        }
    }
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem == newItem
    }
}
