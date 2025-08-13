package iss.nus.edu.sg.feature_saveroute

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.databinding.RouteslistviewBinding

class MyCustomAdapter(
    private val plans: List<CommutePlan>,
    private val onEditClick: (CommutePlan, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MyCustomAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(val binding: RouteslistviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = RouteslistviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun getItemCount() = plans.size

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val plan = plans[position]
        val b = holder.binding

        b.routeNumber.text = "Route ${position + 1}"

        val titleFrom = plan.startLocationId ?: "-"
        val titleTo = plan.endLocationId ?: "-"
        b.FromText.text = titleFrom
        b.DestinationText.text = titleTo

        b.BusStop.text = "Bus Stop: ${plan.busStop ?: "-"}"
        b.BusService.text = "Bus Service: ${plan.busService ?: "-"}"

        b.StartTime.text = "Start at: ${plan.notifyAt ?: "-"}"
        b.ArrivalTime.text = "Arrive at: ${plan.arrivalTime ?: "-"}"

        // Better frequency formatting with logging
        val days = plan.selectedDays
        val recurrence = plan.recurrence == true
        val frequencyText = formatDays(days, recurrence)

        Log.d("Adapter", "Plan ${plan.id}: selectedDays=${days?.contentToString()}, recurrence=$recurrence, frequency=$frequencyText")

        b.frequency.text = "Frequency: $frequencyText"

        b.editRouteButton.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onEditClick(plan, pos)
        }
        b.deleteRoute.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onDeleteClick(pos)
        }
    }

    private fun formatDays(days: BooleanArray?, recurrence: Boolean): String {
        if (!recurrence || days == null) {
            return "Once"
        }

        // Ensure we have at least 7 elements, pad with false if needed
        val paddedDays = if (days.size < 7) {
            days + BooleanArray(7 - days.size) { false }
        } else {
            days.take(7).toBooleanArray()
        }

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val selectedDayNames = dayNames.filterIndexed { i, _ -> i < paddedDays.size && paddedDays[i] }

        return when {
            selectedDayNames.isEmpty() -> "No days selected"
            selectedDayNames.size == 7 -> "Everyday"
            selectedDayNames == dayNames.subList(0, 5) -> "Mon - Fri" // Weekdays
            selectedDayNames == dayNames.subList(5, 7) -> "Weekends" // Sat, Sun
            else -> selectedDayNames.joinToString(", ")
        }
    }
}