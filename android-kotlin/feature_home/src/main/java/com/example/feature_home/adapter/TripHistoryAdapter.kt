package com.example.feature_home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.Trip
import com.example.core.model.TripStatus
import com.example.feature_home.databinding.ItemTripHistoryBinding
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class TripHistoryAdapter(
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripHistoryAdapter.TripHistoryViewHolder>() {

    private var trips = listOf<Trip>()

    fun updateTrips(newTrips: List<Trip>) {
        val oldTrips = trips
        trips = newTrips.sortedByDescending { parseDateTime(it.startTime) }
        
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldTrips.size
            override fun getNewListSize() = trips.size
            
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTrips[oldItemPosition].id == trips[newItemPosition].id
            }
            
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTrips[oldItemPosition] == trips[newItemPosition]
            }
        })
        
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripHistoryViewHolder {
        val binding = ItemTripHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripHistoryViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    inner class TripHistoryViewHolder(
        private val binding: ItemTripHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip) {
            binding.apply {
                // Set trip locations
                tvStartLocation.text = trip.startLocation
                tvEndLocation.text = trip.endLocation

                // Format and set dates/times
                try {
                    val startDateTime = parseDateTime(trip.startTime)
                    val endDateTime = trip.endTime?.let { parseDateTime(it) }

                    // Format date
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                    tvTripDate.text = startDateTime.format(dateFormatter)

                    // Format time range
                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                    val startTimeStr = startDateTime.format(timeFormatter)
                    val endTimeStr = endDateTime?.format(timeFormatter) ?: "Incomplete"
                    tvTripTime.text = "$startTimeStr - $endTimeStr"

                    // Calculate and set duration
                    val duration = if (endDateTime != null) {
                        val minutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime)
                        when {
                            minutes < 60 -> "${minutes} min"
                            minutes < 1440 -> {
                                val hours = minutes / 60
                                val remainingMinutes = minutes % 60
                                if (remainingMinutes == 0L) "${hours}h" 
                                else "${hours}h ${remainingMinutes}m"
                            }
                            else -> {
                                val days = minutes / 1440
                                "${days}d"
                            }
                        }
                    } else {
                        when (trip.status) {
                            TripStatus.ON_TRIP -> "In Progress"
                            TripStatus.COMPLETED -> "Unknown"
                        }
                    }
                    tvTripDuration.text = duration

                } catch (e: Exception) {
                    // Fallback for parsing errors
                    tvTripDate.text = "Unknown Date"
                    tvTripTime.text = "Unknown Time"
                    tvTripDuration.text = "Unknown"
                }

                // Create route summary
                val routeSummary = trip.route.legs.joinToString(" • ") { leg ->
                    when (leg.type.uppercase()) {
                        "WALK" -> "Walk"
                        "BUS" -> "Bus ${leg.busServiceNumber ?: ""}"
                        else -> leg.type.replaceFirstChar { it.titlecase() }
                    }
                }
                tvRouteSummary.text = routeSummary

                // Set click listener
                root.setOnClickListener {
                    onTripClick(trip)
                }
            }
        }
    }

    private fun parseDateTime(dateTimeString: String): OffsetDateTime {
        return try {
            OffsetDateTime.parse(dateTimeString)
        } catch (e: Exception) {
            // Fallback parsing attempts
            try {
                OffsetDateTime.parse(dateTimeString + "Z")
            } catch (e2: Exception) {
                // Return current time as fallback
                OffsetDateTime.now()
            }
        }
    }
}