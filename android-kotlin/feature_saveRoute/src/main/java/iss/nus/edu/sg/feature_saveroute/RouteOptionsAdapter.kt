package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.Route
import com.example.core.model.RouteLeg
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import iss.nus.edu.sg.feature_saveroute.databinding.ItemRouteOptionBinding

class RouteOptionsAdapter(
    private val onRouteSelected: (Route) -> Unit
) : RecyclerView.Adapter<RouteOptionsAdapter.RouteViewHolder>() {

    private var routes = listOf<Route>()

    fun updateRoutes(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount() = routes.size

    inner class RouteViewHolder(
        private val binding: ItemRouteOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route) {
            binding.routeTotalDurationTextView.text = "${route.durationInMinutes} min"
            
            // Clear previous route legs and populate with chips
            populateRouteLegs(binding.routeLegsFlexboxLayout, route.legs)
            
            binding.root.setOnClickListener {
                onRouteSelected(route)
            }
        }
        
        private fun populateRouteLegs(flexboxLayout: FlexboxLayout, legs: List<RouteLeg>) {
            flexboxLayout.removeAllViews()
            val context = flexboxLayout.context

            legs.forEachIndexed { index, leg ->
                val chip = createLegChip(context, leg)
                flexboxLayout.addView(chip)

                if (index < legs.lastIndex) {
                    val separator = createSeparatorView(context)
                    flexboxLayout.addView(separator)
                }
            }
        }
        
        private fun createLegChip(context: Context, leg: RouteLeg): Chip {
            return Chip(context).apply {
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
                    chip.text = "${leg.durationInMinutes}m"
                    chip.setChipIconResource(R.drawable.ic_walk)
                    chip.chipBackgroundColor = ColorStateList.valueOf("#5F3A15".toColorInt())
                    chip.setTextColor(Color.WHITE)
                }
                "BUS" -> {
                    chip.text = leg.busServiceNumber ?: "Bus"
                    chip.setChipIconResource(R.drawable.ic_bus)
                    chip.chipBackgroundColor = ColorStateList.valueOf("#718C0F".toColorInt())
                    chip.setTextColor(Color.WHITE)
                }
                else -> {
                    val transportType = leg.type.lowercase().replaceFirstChar(Char::titlecase)
                    chip.text = "${transportType} ${leg.durationInMinutes}m"
                    chip.setChipIconResource(R.drawable.ic_walk) // Default icon
                    chip.chipBackgroundColor = ColorStateList.valueOf(
                        chip.context.getColor(com.google.android.material.R.color.mtrl_chip_background_color)
                    )
                    chip.setTextColor(Color.BLACK)
                }
            }
            chip.chipIconTint = null
        }

        private fun createSeparatorView(context: Context): ImageView {
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
}