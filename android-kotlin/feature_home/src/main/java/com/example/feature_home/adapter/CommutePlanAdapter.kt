package com.example.feature_home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.core.api.CommutePlan
import com.example.feature_home.databinding.ItemCommutePlanBinding

class CommutePlanAdapter(
    private var commutePlans: List<CommutePlan> = emptyList()
) : RecyclerView.Adapter<CommutePlanAdapter.CommutePlanViewHolder>() {

    fun updateCommutes(newPlans: List<CommutePlan>) {
        commutePlans = newPlans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommutePlanViewHolder {
        val binding = ItemCommutePlanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommutePlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommutePlanViewHolder, position: Int) {
        holder.bind(commutePlans[position])
    }

    override fun getItemCount(): Int = commutePlans.size

    class CommutePlanViewHolder(
        private val binding: ItemCommutePlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(commutePlan: CommutePlan) {
            // Commute name
            binding.commuteName.text = commutePlan.commutePlanName

            // Always label as "Starts"
            binding.startTimeLabel.text = "Starts"

            // Show notify time in HH:mm or fallback
            binding.startTimeValue.text = formatTime(commutePlan.notifyAt)
        }

        private fun formatTime(timeString: String?): String {
            return if (timeString.isNullOrEmpty()) {
                "Not set"
            } else {
                try {
                    timeString.substring(0, minOf(5, timeString.length))
                } catch (e: Exception) {
                    timeString
                }
            }
        }
    }
}
