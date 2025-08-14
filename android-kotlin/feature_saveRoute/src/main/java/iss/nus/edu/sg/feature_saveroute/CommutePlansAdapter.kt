package iss.nus.edu.sg.feature_saveroute

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.core.api.CommutePlan
import com.example.core.api.SavedLocation

class CommutePlansAdapter(
    private var commutePlans: MutableList<CommutePlan>,
    private var savedLocations: List<SavedLocation>,
    private val onEditClick: (CommutePlan, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CommutePlansAdapter.CommutePlanViewHolder>() {

    companion object {
        private const val TAG = "CommutePlansAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommutePlanViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.routeslistview, parent, false)
        return CommutePlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommutePlanViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder called for position $position")
        holder.bind(commutePlans[position], position)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount() returning ${commutePlans.size}")
        return commutePlans.size
    }

    fun updateCommutePlans(newCommutePlans: List<CommutePlan>) {
        Log.d(TAG, "updateCommutePlans called with ${newCommutePlans.size} plans")
        commutePlans.clear()
        commutePlans.addAll(newCommutePlans)
        Log.d(TAG, "After update, commutePlans size: ${commutePlans.size}")
        notifyDataSetChanged()
    }

    fun updateLocations(newLocations: List<SavedLocation>) {
        Log.d(TAG, "updateLocations called with ${newLocations.size} locations")
        savedLocations = newLocations
        notifyDataSetChanged()
    }

    inner class CommutePlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val routeNumber: TextView = itemView.findViewById(R.id.routeNumber)
        private val frequency: TextView = itemView.findViewById(R.id.frequency)
        private val fromText: TextView = itemView.findViewById(R.id.FromText)
        private val destinationText: TextView = itemView.findViewById(R.id.DestinationText)
        private val startTime: TextView = itemView.findViewById(R.id.StartTime)
        private val busStop: TextView = itemView.findViewById(R.id.BusStop)
        private val busService: TextView = itemView.findViewById(R.id.BusService)
        private val arrivalTime: TextView = itemView.findViewById(R.id.ArrivalTime)
        private val editButton: Button = itemView.findViewById(R.id.editRouteButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteRoute)

        fun bind(commutePlan: CommutePlan, position: Int) {
            Log.d(TAG, "Binding position $position: ${commutePlan.commutePlanName}")
            
            // Find location names from IDs
            val startLocation = savedLocations.find { it.id == commutePlan.startLocationId }
            val endLocation = savedLocations.find { it.id == commutePlan.endLocationId }
            
            val fromName = startLocation?.locationName ?: "Unknown Location"
            val toName = endLocation?.locationName ?: "Unknown Location"
            
            Log.d(TAG, "Start Location: $fromName, End Location: $toName")
            Log.d(TAG, "Available locations: ${savedLocations.map { it.locationName }}")
            
            // Set the route name/title
            routeNumber.text = commutePlan.commutePlanName.ifEmpty { "Commute Plan" }
            
            // Show frequency/recurrence
            frequency.text = if (commutePlan.recurrence == true) "Recurring" else "One-time"
            
            // Set from and to locations
            fromText.text = "From: $fromName"
            destinationText.text = "To: $toName"
            
            // Set start time
            startTime.text = "Start at: ${commutePlan.notifyAt}"
            
            // For now, hide bus stop and service (these are replaced by route selection)
            busStop.visibility = View.GONE
            busService.visibility = View.GONE
            
            // Set arrival time if available
            if (!commutePlan.arrivalTime.isNullOrEmpty()) {
                arrivalTime.text = "Arrive by: ${commutePlan.arrivalTime}"
                arrivalTime.visibility = View.VISIBLE
            } else {
                arrivalTime.visibility = View.GONE
            }

            editButton.setOnClickListener {
                onEditClick(commutePlan, position)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }
}