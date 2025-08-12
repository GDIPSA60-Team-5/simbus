package com.example.feature_chatbot.domain.view_holders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.example.feature_chatbot.R
import com.example.feature_chatbot.data.BotResponse
import com.example.core.model.Route
import com.example.core.model.RouteLeg
import com.example.core.model.Coordinates
import com.example.feature_chatbot.databinding.ItemChatBotDirectionsBinding
import com.google.android.material.chip.Chip
import com.example.feature_chatbot.BuildConfig
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.example.feature_guidemap.MapsNavigationActivity
import java.net.URLEncoder

class BotDirectionsViewHolder(private val binding: ItemChatBotDirectionsBinding) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val ARROW_SIZE_DP = 32
        private const val ARROW_PADDING_DP = 1
    }

    private val staticMapImageView: ImageView = binding.routeStaticMapView

    private var selectedRouteIndex: Int = -1
    private var allRoutes: List<Route> = emptyList()
    private var startCoordinates: Coordinates? = null
    private var endCoordinates: Coordinates? = null

    fun bind(directions: BotResponse.Directions) {
        startCoordinates = directions.startCoordinates
        endCoordinates = directions.endCoordinates

        bindLocationInfo(directions)
        bindRoutes(directions.suggestedRoutes)

        allRoutes = directions.suggestedRoutes ?: emptyList()
        selectedRouteIndex = 0
        updateRouteSelection()
        showSelectedRouteOnStaticMap()

        // Set Start Journey button click listener here
        binding.startJourneyButton.setOnClickListener {
            sendSelectedRouteToMapsNavigation()
        }
    }

    private fun sendSelectedRouteToMapsNavigation() {
        if (selectedRouteIndex !in allRoutes.indices) return

        val selectedRoute = allRoutes[selectedRouteIndex]
        val context = itemView.context

        val intent = android.content.Intent(context, MapsNavigationActivity::class.java)
        intent.putExtra("selected_route", selectedRoute)
        context.startActivity(intent)
    }


    private fun showSelectedRouteOnStaticMap() {
        if (selectedRouteIndex !in allRoutes.indices) return

        val selectedRoute = allRoutes[selectedRouteIndex]
        val url = buildStaticMapUrl(selectedRoute)

        Glide.with(itemView.context)
            .load(url)
            .placeholder(R.drawable.placeholder_map) // your placeholder drawable
            .error(R.drawable.error_map) // your error drawable
            .into(staticMapImageView)
            
        // Add click listener to navigate to full map view
        staticMapImageView.setOnClickListener {
            sendSelectedRouteToMapsNavigation()
        }
    }

    private fun buildStaticMapUrl(route: Route): String {
        val baseUrl = "https://maps.googleapis.com/maps/api/staticmap"
        val size = "800x400" // Larger size for better visibility
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        
        // Modern color scheme for different transport modes
        val walkColor = "4285F4" // Google Blue for walking
        val busColor = "34A853" // Google Green for bus
        val defaultColor = "EA4335" // Google Red for other modes
        
        val legsToShow = route.legs
        val allMarkers = mutableListOf<String>()
        val allPaths = mutableListOf<String>()

        // Add start marker with custom styling
        val startLat = startCoordinates?.latitude ?: 0.0
        val startLng = startCoordinates?.longitude ?: 0.0
        allMarkers.add("markers=color:0x4285F4|size:mid|label:S|$startLat,$startLng")

        // Process each leg for paths and bus stop markers
        legsToShow.forEachIndexed { index, leg ->
            val color = when (leg.type.uppercase()) {
                "WALK" -> walkColor
                "BUS" -> busColor
                else -> defaultColor
            }
            
            val weight = if (leg.type.uppercase() == "BUS") 8 else 6 // Thicker lines for bus routes
            
            // Create path for this leg
            val routePoints = leg.routePoints
            if (routePoints != null && routePoints.isNotEmpty()) {
                val pathString = routePoints.joinToString("|") { "${it.latitude},${it.longitude}" }
                allPaths.add("path=color:0x$color|weight:$weight|$pathString")
                
                // Add bus stop markers for transit legs
                if (leg.type.uppercase() == "BUS" && routePoints.size > 2) {
                    // Add markers for intermediate bus stops (excluding start and end points of the leg)
                    routePoints.drop(1).dropLast(1).forEachIndexed { stopIndex, point ->
                        val busStopLabel = leg.busServiceNumber?.take(2) ?: "B" // Use bus number or "B"
                        allMarkers.add("markers=color:0x34A853|size:small|label:$busStopLabel|${point.latitude},${point.longitude}")
                    }
                }
            } else {
                // Fallback to polyline with modern styling
                val safePolyline = URLEncoder.encode(leg.legGeometry, "UTF-8")
                allPaths.add("path=color:0x$color|weight:$weight|enc:$safePolyline")
            }
        }

        // Add end marker with custom styling
        val endLat = endCoordinates?.latitude ?: 0.0
        val endLng = endCoordinates?.longitude ?: 0.0
        allMarkers.add("markers=color:0xEA4335|size:mid|label:E|$endLat,$endLng")
        
        // Add transfer points markers for multi-leg routes
        if (legsToShow.size > 1) {
            for (i in 0 until legsToShow.size - 1) {
                val currentLeg = legsToShow[i]
                val nextLeg = legsToShow[i + 1]
                
                // Get the end point of current leg (transfer point)
                val transferPoint = currentLeg.routePoints?.lastOrNull()
                if (transferPoint != null && 
                    currentLeg.type.uppercase() != "WALK" && 
                    nextLeg.type.uppercase() != "WALK") {
                    allMarkers.add("markers=color:0xFBBC04|size:small|label:T|${transferPoint.latitude},${transferPoint.longitude}")
                }
            }
        }

        // Combine all components
        val pathParams = allPaths.joinToString("&")
        val markerParams = allMarkers.joinToString("&")
        
        // Add map styling for modern look
        val style = "style=feature:all|element:geometry|color:0xf5f5f5&" +
                   "style=feature:water|element:all|color:0xc9c9c9&" +
                   "style=feature:road|element:all|color:0xffffff"

        return "$baseUrl?size=$size&$pathParams&$markerParams&$style&key=$apiKey"
    }


    private fun bindLocationInfo(directions: BotResponse.Directions) {
        binding.startLocationTextView.text =
            itemView.context.getString(R.string.from_location, directions.startLocation)
        binding.endLocationTextView.text =
            itemView.context.getString(R.string.to_location, directions.endLocation)
    }

    private fun bindRoutes(routes: List<Route>?) {
        binding.routesContainer.removeAllViews()

        if (routes.isNullOrEmpty()) {
            addNoRoutesMessage()
        } else {
            routes.forEachIndexed { i, route -> addRouteView(route, i) }
        }
    }

    private fun addNoRoutesMessage() {
        val noRoutesTv = android.widget.TextView(itemView.context).apply {
            text = itemView.context.getString(R.string.no_routes_found)
        }
        binding.routesContainer.addView(noRoutesTv)
    }

    private fun addRouteView(route: Route, index: Int) {
        val inflater = LayoutInflater.from(itemView.context)
        val routeViewBinding =
            com.example.feature_chatbot.databinding.ItemRouteSuggestionBinding.inflate(
                inflater,
                binding.routesContainer,
                false
            )

        routeViewBinding.routeTotalDurationTextView.text = route.durationInMinutes.toString()
        populateRouteLegs(routeViewBinding.routeLegsFlexboxLayout, route.legs)

        routeViewBinding.root.setOnClickListener {
            selectedRouteIndex = index
            updateRouteSelection()
            showSelectedRouteOnStaticMap()
        }

        binding.routesContainer.addView(routeViewBinding.root)
    }

    private fun updateRouteSelection() {
        for (i in 0 until binding.routesContainer.childCount) {
            val child = binding.routesContainer.getChildAt(i)
            child.isSelected = (i == selectedRouteIndex)
        }
    }

    private fun populateRouteLegs(flexboxLayout: FlexboxLayout, legs: List<RouteLeg>) {
        flexboxLayout.removeAllViews()
        val context = flexboxLayout.context

        legs.forEachIndexed { index, leg ->
            val chip = createLegChip(context, leg)
            flexboxLayout.addView(chip)

            if (index < legs.lastIndex) {
                val separator = createSeparatorView()
                flexboxLayout.addView(separator)
            }
        }
    }
    private fun createLegChip(context: Context, leg: RouteLeg): Chip {
        return (LayoutInflater.from(context)
            .inflate(R.layout.route_leg_chip, null, false) as Chip).apply {

            isClickable = false
            isCheckable = false
            chipIconTint = null

            configureChipForLegType(this, leg)
            contentDescription = text
        }
    }

    private fun configureChipForLegType(chip: Chip, leg: RouteLeg) {
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
                val transportType =
                    leg.type.lowercase().replaceFirstChar(Char::titlecase)
                chip.text =
                    chip.context.getString(R.string.transport_duration, transportType, leg.durationInMinutes)
                chip.setChipIconResource(R.drawable.ic_walk) // Default icon
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    chip.context.getColor(com.google.android.material.R.color.mtrl_chip_background_color)
                )
                chip.setTextColor(Color.BLACK)
            }
        }

        chip.chipIconTint = null
    }

    private fun createSeparatorView(): ImageView {
        val context = itemView.context
        return ImageView(context).apply {
            setImageResource(R.drawable.ic_dot_separator)
            val lp = FlexboxLayout.LayoutParams(
                8.dpToPx(context),
                8.dpToPx(context)
            ).apply {
                topMargin = 8.dpToPx(context)
                bottomMargin = 8.dpToPx(context)
                marginStart = 4.dpToPx(context)
                marginEnd = 4.dpToPx(context)
                alignSelf = AlignItems.CENTER
            }
            layoutParams = lp
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }


    // Extension function for dp to px conversion
    private fun Int.dpToPx(context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
}
