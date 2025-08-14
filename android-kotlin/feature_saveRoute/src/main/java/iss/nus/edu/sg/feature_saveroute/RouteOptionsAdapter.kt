package iss.nus.edu.sg.feature_saveroute

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.Route
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
            binding.routeTotalDurationTextView.text = route.durationInMinutes.toString()
            
            // Clear previous route legs
            binding.routeLegsFlexboxLayout.removeAllViews()
            
            // Add route legs to flexbox layout
            route.legs.forEach { leg ->
                val chipView = LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.route_leg_chip, binding.routeLegsFlexboxLayout, false)
                
                // Set chip text based on leg type
                val chipText = when (leg.type) {
                    "BUS" -> leg.busServiceNumber ?: "Bus"
                    "WALK" -> "Walk"
                    else -> leg.type
                }
                
                chipView.findViewById<android.widget.TextView>(R.id.chipText)?.text = chipText
                binding.routeLegsFlexboxLayout.addView(chipView)
            }
            
            binding.root.setOnClickListener {
                onRouteSelected(route)
            }
        }
    }
}