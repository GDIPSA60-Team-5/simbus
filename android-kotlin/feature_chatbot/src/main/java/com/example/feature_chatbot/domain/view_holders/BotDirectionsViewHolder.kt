package com.example.feature_chatbot.domain.view_holders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import com.example.feature_chatbot.R
import com.example.feature_chatbot.data.BotResponse
import com.example.feature_chatbot.data.Route
import com.example.feature_chatbot.data.RouteStep
import com.example.feature_chatbot.databinding.ItemChatBotDirectionsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BotDirectionsViewHolder(private val binding: ItemChatBotDirectionsBinding) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val ARROW_SIZE_DP = 32
        private const val ARROW_PADDING_DP = 1
    }

    fun bind(directions: BotResponse.Directions) {
        bindLocationInfo(directions)
        bindRoutes(directions.suggestedRoutes)
    }

    private fun bindLocationInfo(directions: BotResponse.Directions) {
        binding.startLocationTextView.text = itemView.context.getString(R.string.from_location, directions.startLocation)
        binding.endLocationTextView.text = itemView.context.getString(R.string.to_location, directions.endLocation)
    }

    private fun bindRoutes(routes: List<Route>?) {
        binding.routesContainer.removeAllViews()

        if (routes.isNullOrEmpty()) {
            addNoRoutesMessage()
        } else {
            routes.forEach { route -> addRouteView(route) }
        }
    }

    private fun addNoRoutesMessage() {
        val noRoutesTv = android.widget.TextView(itemView.context).apply {
            text = itemView.context.getString(R.string.no_routes_found)
        }
        binding.routesContainer.addView(noRoutesTv)
    }

    private fun addRouteView(route: Route) {
        val inflater = LayoutInflater.from(itemView.context)
        val routeViewBinding = com.example.feature_chatbot.databinding.ItemRouteSuggestionBinding.inflate(inflater, binding.routesContainer, false)

        routeViewBinding.routeTotalDurationTextView.text = route.durationInMinutes.toString()
        populateRouteLegs(routeViewBinding.routeLegsChipGroup, route.legs)

        binding.routesContainer.addView(routeViewBinding.root)
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

        chip.chipIconTint = null
    }

    private fun createArrowView(): ImageView {
        val context = itemView.context
        val arrow = ImageView(context).apply {
            setImageResource(R.drawable.ic_arrow_right)
            layoutParams = ChipGroup.LayoutParams(
                /* width */ ARROW_SIZE_DP.dpToPx(context),
                /* height */ LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                // horizontal spacing
                marginStart = ARROW_PADDING_DP.dpToPx(context)
                marginEnd = ARROW_PADDING_DP.dpToPx(context)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            adjustViewBounds = false
        }
        return arrow
    }

    // Extension function moved here to help arrow view creation
    private fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
