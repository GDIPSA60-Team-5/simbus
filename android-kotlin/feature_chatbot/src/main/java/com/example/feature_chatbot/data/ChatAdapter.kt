package com.example.feature_chatbot.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.feature_chatbot.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.core.graphics.toColorInt

class ChatAdapter(
    initialItems: List<ChatItem> = emptyList(),
    private val onMessageAdded: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ARROW_SIZE_DP = 32
        private const val ARROW_PADDING_DP = 4
    }

    private enum class ChatViewType(val layoutRes: Int) {
        GREETING(R.layout.item_greeting),
        USER_MESSAGE(R.layout.item_chat_user),
        BOT_MESSAGE_TEXT(R.layout.item_chat_bot),
        BOT_MESSAGE_DIRECTIONS(R.layout.item_chat_bot_directions),
        BOT_MESSAGE_ERROR(R.layout.item_chat_bot_error),
        TYPING_INDICATOR(R.layout.item_typing_indicator)
    }

    private val items = mutableListOf<ChatItem>().apply {
        add(ChatItem.Greeting)
        addAll(initialItems)
    }

    // --- ViewHolders ---

    inner class GreetingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.greeting_title)
        private val subtitle: TextView = view.findViewById(R.id.greeting_subtitle)

        fun bind() {
            title.text = itemView.context.getString(R.string.greeting_title, "Aung")
            subtitle.text = itemView.context.getString(R.string.greeting_subtitle)
        }
    }

    inner class UserMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.messageText)

        fun bind(text: String) {
            messageText.text = text
        }
    }

    inner class BotTextMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.messageText)

        fun bind(text: String) {
            messageText.text = text
        }
    }

    inner class BotDirectionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val startLocationTv: TextView = view.findViewById(R.id.startLocationTextView)
        private val endLocationTv: TextView  = view.findViewById(R.id.endLocationTextView)
        private val routesContainer: LinearLayout =view.findViewById(R.id.routesContainer)

        fun bind(directions: BotResponse.Directions) {
            bindLocationInfo(directions)
            bindRoutes(directions.suggestedRoutes)
        }

        private fun bindLocationInfo(directions: BotResponse.Directions) {
            startLocationTv.text = itemView.context.getString(R.string.from_location, directions.startLocation)
            endLocationTv.text = itemView.context.getString(R.string.to_location, directions.endLocation)
        }

        private fun bindRoutes(routes: List<Route>?) {
            routesContainer.removeAllViews()

            if (routes.isNullOrEmpty()) {
                addNoRoutesMessage()
            } else {
                routes.forEach { route -> addRouteView(route) }
            }
        }

        private fun addNoRoutesMessage() {
            val noRoutesTv = TextView(itemView.context).apply {
                text = itemView.context.getString(R.string.no_routes_found)
            }
            routesContainer.addView(noRoutesTv)
        }

        private fun addRouteView(route: Route) {
            val routeView = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_route_suggestion, routesContainer, false)

            val routeDuration: TextView = routeView.findViewById(R.id.routeTotalDurationTextView)
            routeDuration.text = route.durationInMinutes.toString()

            val routeLegsChipGroup: ChipGroup = routeView.findViewById(R.id.routeLegsChipGroup)
            populateRouteLegs(routeLegsChipGroup, route.legs)

            routesContainer.addView(routeView)
        }

        private fun populateRouteLegs(chipGroup: ChipGroup, legs: List<RouteStep>) {
            chipGroup.removeAllViews()
            val context = chipGroup.context

            legs.forEachIndexed { index, leg ->
                val chip = createLegChip(context, leg)
                chipGroup.addView(chip)

                if (index < legs.lastIndex) {
                    val arrow = createArrowView()
                    chipGroup.addView(arrow)
                }
            }
        }


        private fun createArrowView(): ImageView {
            return ImageView(itemView.context).apply {
                setImageResource(R.drawable.ic_arrow_right)
                val size = ARROW_SIZE_DP.dpToPx(context)
                val padding = ARROW_PADDING_DP.dpToPx(context)

                // Use LinearLayout.LayoutParams and center vertical
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setMargins(padding, 0, padding, 0)
                }

                // Remove padding on ImageView itself; use margins instead
                setPadding(0, 0, 0, 0)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }

    }

    inner class BotErrorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val errorText: TextView = view.findViewById(R.id.errorText)

        fun bind(errorMessage: String) {
            errorText.text = errorMessage
        }
    }

    inner class TypingIndicatorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val typingText: TextView = view.findViewById(R.id.typingText)

        fun bind(message: String) {
            typingText.text = message
        }
    }

    // --- Adapter Methods ---

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatItem.Greeting -> ChatViewType.GREETING
            is ChatItem.UserMessage -> ChatViewType.USER_MESSAGE
            is ChatItem.TypingIndicator -> ChatViewType.TYPING_INDICATOR
            is ChatItem.BotMessage -> {
                when (item.botResponse) {
                    is BotResponse.Message -> ChatViewType.BOT_MESSAGE_TEXT
                    is BotResponse.Directions -> ChatViewType.BOT_MESSAGE_DIRECTIONS
                    is BotResponse.Error -> ChatViewType.BOT_MESSAGE_ERROR
                }
            }
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val chatViewType = ChatViewType.entries[viewType]
        val view = inflater.inflate(chatViewType.layoutRes, parent, false)

        return when (chatViewType) {
            ChatViewType.GREETING -> GreetingViewHolder(view)
            ChatViewType.USER_MESSAGE -> UserMessageViewHolder(view)
            ChatViewType.BOT_MESSAGE_TEXT -> BotTextMessageViewHolder(view)
            ChatViewType.BOT_MESSAGE_DIRECTIONS -> BotDirectionsViewHolder(view)
            ChatViewType.BOT_MESSAGE_ERROR -> BotErrorViewHolder(view)
            ChatViewType.TYPING_INDICATOR -> TypingIndicatorViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatItem.Greeting -> (holder as GreetingViewHolder).bind()
            is ChatItem.UserMessage -> (holder as UserMessageViewHolder).bind(item.text)
            is ChatItem.TypingIndicator -> (holder as TypingIndicatorViewHolder).bind(item.message)
            is ChatItem.BotMessage -> bindBotMessage(holder, item)
        }
    }

    private fun bindBotMessage(holder: RecyclerView.ViewHolder, item: ChatItem.BotMessage) {
        when (val botResponse = item.botResponse) {
            is BotResponse.Message -> (holder as BotTextMessageViewHolder).bind(botResponse.text)
            is BotResponse.Directions -> (holder as BotDirectionsViewHolder).bind(botResponse)
            is BotResponse.Error -> (holder as BotErrorViewHolder).bind(botResponse.message)
        }
    }

    // --- Private Helper Methods ---

    private fun createLegChip(context: Context, leg: RouteStep): Chip {
        return (LayoutInflater.from(context)
            .inflate(R.layout.route_leg_chip, null, false) as Chip).apply {

            isClickable = false
            isCheckable = false
            chipIconTint = null

            configureChipForLegType(this, leg)
            contentDescription = text
        }
    }


    private fun configureChipForLegType(chip: Chip, leg: RouteStep) {
        when (leg.type.uppercase()) {
            "WALK" -> {
                chip.text = leg.durationInMinutes.toString()
                chip.setChipIconResource(R.drawable.ic_walk)
                chip.chipBackgroundColor = ColorStateList.valueOf("#5F3A15".toColorInt())
                chip.setTextColor(Color.WHITE)
            }
            "BUS" -> {
                chip.text = leg.busServiceNumber
                chip.setChipIconResource(R.drawable.ic_bus)
                chip.chipBackgroundColor = ColorStateList.valueOf("#718C0F".toColorInt())
                chip.setTextColor(Color.WHITE)
            }
            else -> {
                val transportType = leg.type.lowercase().replaceFirstChar(Char::titlecase)
                chip.text = chip.context.getString(R.string.transport_duration, transportType, leg.durationInMinutes)
                chip.setChipIconResource(R.drawable.ic_walk) // Default icon
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    chip.context.getColor(com.google.android.material.R.color.mtrl_chip_background_color)
                )
                chip.setTextColor(Color.BLACK)
            }
        }

        // preserve icon tint if needed
        chip.chipIconTint = null
    }



    // --- Public Methods ---

    fun addChatItem(item: ChatItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
        onMessageAdded?.invoke()
    }

    fun replaceLastChatItem(newItem: ChatItem) {
        if (items.isNotEmpty()) {
            val lastIndex = items.size - 1
            items[lastIndex] = newItem
            notifyItemChanged(lastIndex)
            onMessageAdded?.invoke()
        } else {
            addChatItem(newItem)
        }
    }

    fun replaceAll(newChatItems: List<ChatItem>) {
        items.clear()
        items.add(ChatItem.Greeting)
        items.addAll(newChatItems)
        notifyDataSetChanged()
    }

    // --- Extension Functions ---

    private fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}