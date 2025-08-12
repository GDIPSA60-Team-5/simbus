package com.example.feature_chatbot.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R
import com.example.feature_chatbot.databinding.*
import com.example.feature_chatbot.domain.view_holders.*
import com.example.feature_chatbot.util.ChatItemDiffCallback

class ChatAdapter(
    onMessageAdded: (() -> Unit)? = null
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatItemDiffCallback()) {

    private val items = mutableListOf<ChatItem>()
    private val onMessageAddedCallback = onMessageAdded
    var username: String = "User"

    private val viewHolderFactories = mapOf(
        ChatViewType.GREETING to { parent: ViewGroup -> GreetingViewHolder(ItemGreetingBinding.inflate(LayoutInflater.from(parent.context), parent, false)) },
        ChatViewType.USER_MESSAGE to { parent: ViewGroup -> UserMessageViewHolder(ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)) },
        ChatViewType.BOT_MESSAGE_TEXT to { parent: ViewGroup -> BotTextMessageViewHolder(ItemChatBotBinding.inflate(LayoutInflater.from(parent.context), parent, false)) },
        ChatViewType.BOT_MESSAGE_DIRECTIONS to { parent: ViewGroup -> BotDirectionsViewHolder(ItemChatBotDirectionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)) },
        ChatViewType.BOT_MESSAGE_ERROR to { parent: ViewGroup -> BotErrorViewHolder(ItemChatBotErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false)) },
        ChatViewType.TYPING_INDICATOR to { parent: ViewGroup -> TypingIndicatorViewHolder(ItemTypingIndicatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)) }
    )

    private enum class ChatViewType(val layoutRes: Int) {
        GREETING(R.layout.item_greeting),
        USER_MESSAGE(R.layout.item_chat_user),
        BOT_MESSAGE_TEXT(R.layout.item_chat_bot),
        BOT_MESSAGE_DIRECTIONS(R.layout.item_chat_bot_directions),
        BOT_MESSAGE_ERROR(R.layout.item_chat_bot_error),
        TYPING_INDICATOR(R.layout.item_typing_indicator)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.Greeting -> ChatViewType.GREETING
            is ChatItem.UserMessage -> ChatViewType.USER_MESSAGE
            is ChatItem.TypingIndicator -> ChatViewType.TYPING_INDICATOR
            is ChatItem.BotMessage -> when (item.botResponse) {
                is BotResponse.Message -> ChatViewType.BOT_MESSAGE_TEXT
                is BotResponse.Directions -> ChatViewType.BOT_MESSAGE_DIRECTIONS
                is BotResponse.Error -> ChatViewType.BOT_MESSAGE_ERROR
            }
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolderFactories[ChatViewType.entries[viewType]]?.invoke(parent)
            ?: throw IllegalArgumentException("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatItem.Greeting -> (holder as GreetingViewHolder).bind(username)
            is ChatItem.UserMessage -> (holder as UserMessageViewHolder).bind(item.text)
            is ChatItem.TypingIndicator -> (holder as TypingIndicatorViewHolder).bind(item.message)
            is ChatItem.BotMessage -> bindBotMessage(holder, item)
        }
    }

    private fun bindBotMessage(holder: RecyclerView.ViewHolder, item: ChatItem.BotMessage) {
        when (val botResponse = item.botResponse) {
            is BotResponse.Message -> (holder as BotTextMessageViewHolder).bind(botResponse.message)
            is BotResponse.Directions -> (holder as BotDirectionsViewHolder).bind(botResponse)
            is BotResponse.Error -> (holder as BotErrorViewHolder).bind(botResponse.message)
        }
    }

    fun addChatItem(item: ChatItem) {
        items.add(item)
        submitList(items.toList()) { onMessageAddedCallback?.invoke() }
    }

    fun replaceLastChatItem(newItem: ChatItem) {
        if (items.isNotEmpty()) {
            items[items.lastIndex] = newItem
        } else {
            items.add(newItem)
        }
        submitList(items.toList()) { onMessageAddedCallback?.invoke() }
    }

    fun replaceAll(newChatItems: List<ChatItem>) {
        items.clear()
        items.add(ChatItem.Greeting)
        items.addAll(newChatItems)
        submitList(items.toList())
    }
}
