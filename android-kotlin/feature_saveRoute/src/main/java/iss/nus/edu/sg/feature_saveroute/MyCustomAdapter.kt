package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.databinding.RouteslistviewBinding

class MyCustomAdapter(
    private val routes: List<Route>,
    private val onEditClick: (Route, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MyCustomAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(val binding: RouteslistviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = RouteslistviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun getItemCount() = routes.size

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        val b = holder.binding

        b.routeNumber.text = "Route ${position + 1}"
        b.FromText.text =  "${route.from}"
        b.DestinationText.text = "${route.to}"
        b.BusStop.text = "Bus Stop: ${route.busStop}"
        b.BusService.text = "Bus Service: ${route.busService}"
        b.StartTime.text = "Start at: ${route.startTime}"
        b.ArrivalTime.text = "Arrive at: ${route.arrivalTime}"
        b.frequency.text = "Frequency: ${formatDays(route.selectedDays ?: BooleanArray(7))}"

        b.editRouteButton.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onEditClick(route, pos)
        }

        b.deleteRoute.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onDeleteClick(pos)
        }

    }

    private fun formatDays(days: BooleanArray): String {
        val nameOfDay = listOf("Mon", "Tue", "Wed", "Thurs", "Fri", "Sat", "Sun")
        return when {
            days.all { it } -> "Everyday"
            days.slice(0..4).all { it } && !days[5] && !days[6] -> "Mon - Fri"
            else -> nameOfDay.filterIndexed { index, _ -> days[index] }.joinToString(", ")
        }
    }
}