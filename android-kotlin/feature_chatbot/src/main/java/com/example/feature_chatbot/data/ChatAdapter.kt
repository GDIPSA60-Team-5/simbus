package com.example.feature_chatbot.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R

class ChatAdapter(
    initialMessages: List<ChatItem.Message>,
    private val onMessageAdded: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GREETING = 0
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
    }

    private val items = mutableListOf<ChatItem>().apply {
        add(ChatItem.Greeting) // always first
        addAll(initialMessages)
    }

    inner class GreetingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.greeting_title)
        private val subtitle: TextView = view.findViewById(R.id.greeting_subtitle)
        fun bind() {
            title.text = "Good Morning, Aung!"
            subtitle.text = "Where can I take you today?"
        }
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
    }

    inner class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatItem.Greeting -> TYPE_GREETING
            is ChatItem.Message -> if ((items[position] as ChatItem.Message).isUser) TYPE_USER else TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GREETING -> {
                val v = inflater.inflate(R.layout.item_greeting, parent, false)
                GreetingViewHolder(v)
            }
            TYPE_USER -> {
                val v = inflater.inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_chat_bot, parent, false)
                BotViewHolder(v)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatItem.Greeting -> (holder as GreetingViewHolder).bind()
            is ChatItem.Message -> {
                if (holder is UserViewHolder) {
                    holder.messageText.text = item.text
                } else if (holder is BotViewHolder) {
                    holder.messageText.text = item.text
                }
            }
        }
    }

    fun addMessage(message: ChatItem.Message) {
        items.add(message)
        notifyItemInserted(items.size - 1)
        onMessageAdded?.invoke()
    }

    fun removeMessage(message: ChatItem.Message) {
        val index = items.indexOf(message)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateMessageAt(index: Int, newText: String) {
        val oldMessage = items[index]
        if (oldMessage is ChatItem.Message) {
            items[index] = oldMessage.copy(text = newText)
            notifyItemChanged(index)
        }
    }


    fun replaceAll(messages: List<ChatItem.Message>) {
        items.clear()
        items.add(ChatItem.Greeting)
        items.addAll(messages)
        notifyDataSetChanged()
    }
}
