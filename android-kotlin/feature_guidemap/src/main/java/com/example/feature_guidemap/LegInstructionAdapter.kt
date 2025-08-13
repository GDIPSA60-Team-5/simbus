package com.example.feature_guidemap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.RouteLeg

class LegInstructionAdapter(
    private val legs: List<RouteLeg>,
    private val onLegSelected: (Int) -> Unit
) : RecyclerView.Adapter<LegInstructionAdapter.LegViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leg_instruction, parent, false)
        return LegViewHolder(view)
    }

    override fun onBindViewHolder(holder: LegViewHolder, position: Int) {
        holder.bind(legs[position], position)
    }

    override fun getItemCount(): Int = legs.size

    inner class LegViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transportIcon: ImageView = itemView.findViewById(R.id.iv_transport_icon)
        private val legInstruction: TextView = itemView.findViewById(R.id.tv_leg_instruction)
        private val legDuration: TextView = itemView.findViewById(R.id.tv_leg_duration)
        private val busInfo: LinearLayout = itemView.findViewById(R.id.ll_bus_info)
        private val busNumber: TextView = itemView.findViewById(R.id.tv_bus_number)
        private val busArrival: TextView = itemView.findViewById(R.id.tv_bus_arrival)
        private val busDestination: TextView = itemView.findViewById(R.id.tv_bus_destination)
        private val stopDetails: LinearLayout = itemView.findViewById(R.id.ll_stop_details)
        private val stopName: TextView = itemView.findViewById(R.id.tv_stop_name)
        private val stopCode: TextView = itemView.findViewById(R.id.tv_stop_code)

        fun bind(leg: RouteLeg, position: Int) {
            // Set transport icon and color based on leg type
            when (leg.type.uppercase()) {
                "WALK" -> {
                    transportIcon.setImageResource(R.drawable.ic_navigation) // Using available icon
                    transportIcon.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.direction_straight)
                }
                "BUS" -> {
                    transportIcon.setImageResource(R.drawable.ic_navigation) // Using available icon
                    transportIcon.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.direction_right)
                }
                else -> {
                    transportIcon.setImageResource(R.drawable.ic_navigation)
                    transportIcon.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.direction_destination)
                }
            }

            // Set instruction text
            legInstruction.text = when (leg.type.uppercase()) {
                "WALK" -> "Walk to ${leg.toStopName ?: "destination"}"
                "BUS" -> "Take Bus ${leg.busServiceNumber} to ${leg.toStopName ?: "destination"}"
                else -> leg.instruction
            }

            // Set duration text
            legDuration.text = "${leg.durationInMinutes} min"

            // Show bus info for bus stops
            if (leg.type.uppercase() == "BUS") {
                busInfo.visibility = View.VISIBLE
                busNumber.text = leg.busServiceNumber ?: "?"
                
                // Dummy bus arrival data
                val arrivalTimes = listOf(2, 7, 12)
                busArrival.text = "${arrivalTimes[0]} min"
                busDestination.text = "to ${leg.toStopName ?: "destination"}"
                
                // Show alternatives - using dummy data for now
                val alternativeBuses = listOf("199", "97")
                val alternativeText = "Alternatives: ${alternativeBuses.joinToString(", ") { "$it (${(5..10).random()} min)" }}"
                val alternativesContainer = itemView.findViewById<LinearLayout>(R.id.ll_alternative_buses)
                if (alternativesContainer.childCount > 0) {
                    val textView = alternativesContainer.getChildAt(0) as? TextView
                    textView?.text = alternativeText
                }
            } else {
                busInfo.visibility = View.GONE
            }

            // Show stop details for transit stops
            if (!leg.fromStopName.isNullOrEmpty() && leg.type.uppercase() != "WALK") {
                stopDetails.visibility = View.VISIBLE
                stopName.text = leg.fromStopName
                stopCode.text = "Stop: ${leg.fromStopCode ?: "N/A"}"
            } else {
                stopDetails.visibility = View.GONE
            }

            // Set click listener to notify map focus change
            itemView.setOnClickListener {
                onLegSelected(position)
            }
        }
    }
}