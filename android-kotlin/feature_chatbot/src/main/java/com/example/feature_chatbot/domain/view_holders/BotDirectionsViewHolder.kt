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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BotDirectionsViewHolder(private val binding: ItemChatBotDirectionsBinding) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

    companion object {
        private const val ARROW_SIZE_DP = 32
        private const val ARROW_PADDING_DP = 1
    }
    private var mapView: MapView = binding.routeMapView
    private var googleMap: GoogleMap? = null
    private var currentPolylines: MutableList<Polyline> = mutableListOf()

    init {
        mapView.onCreate(null)
        mapView.getMapAsync(this)
    }
    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onDestroy() {
        mapView.onDestroy()
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
    }
    fun bind(directions: BotResponse.Directions) {
        bindLocationInfo(directions)
        bindRoutes(directions.suggestedRoutes)
        if (googleMap != null) {
            drawRoutesOnMap(directions.suggestedRoutes)
        }
    }
    private fun drawRoutesOnMap(routes: List<Route>?) {
        googleMap?.let { map ->
            map.clear()
            currentPolylines.clear()

            if (routes.isNullOrEmpty()) return

            routes.forEach { route ->
                route.routeGeometry?.let { encodedPolyline ->
                    val points = decodePolyline(encodedPolyline)
                    if (points.isNotEmpty()) {
                        val polyline = map.addPolyline(
                            PolylineOptions()
                                .addAll(points)
                                .color(getColorForRoute(route))
                                .width(8f)
                        )
                        currentPolylines.add(polyline)
                    }
                }
            }

            // Move camera to first pointof first route's geometry
            val firstRoute = routes.firstOrNull()
            val firstPoint = firstRoute?.routeGeometry?.let {
                decodePolyline(it).firstOrNull()
            }
            firstPoint?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 13f))
            }
        }
    }


    private fun getColorForRoute(route: Route): Int {
        // Simple example: color based on route summary hashcode
        val colors = listOf(
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#FF5722")  // Deep Orange
        )
        return colors[Math.abs(route.summary.hashCode()) % colors.size]
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

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
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
