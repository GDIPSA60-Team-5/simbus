package com.example.feature_chatbot.domain.view_holders

import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R
import com.example.feature_chatbot.databinding.ItemChatBotBinding
import com.example.feature_chatbot.databinding.ItemChatBotErrorBinding
import com.example.feature_chatbot.databinding.ItemChatUserBinding
import com.example.feature_chatbot.databinding.ItemGreetingBinding
import com.example.feature_chatbot.databinding.ItemTypingIndicatorBinding

class BotTextMessageViewHolder(private val binding: ItemChatBotBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.messageText.text = text
    }
}

class BotErrorViewHolder(private val binding: ItemChatBotErrorBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: String) {
        binding.errorText.text = message
    }
}

class GreetingViewHolder(private val binding: ItemGreetingBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(username: String = "User") {
        binding.greetingTitle.text = itemView.context.getString(R.string.greeting_title, username)
        binding.greetingSubtitle.text = itemView.context.getString(R.string.greeting_subtitle)
    }
}

class UserMessageViewHolder(private val binding: ItemChatUserBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(text: String) {
        binding.messageText.text = text
    }
}

class TypingIndicatorViewHolder(private val binding: ItemTypingIndicatorBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(message: String) {
        binding.typingText.text = message
}
}