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
            binding.commuteName.text = commutePlan.commutePlanName
            binding.notifyTime.text = formatTime(commutePlan.notifyAt)
            binding.arriveTime.text = formatTime(commutePlan.arrivalTime)
        }

        private fun formatTime(timeString: String): String {
            return try {
                // Remove seconds if present (e.g., "07:30:00" -> "07:30")
                timeString.substring(0, 5)
            } catch (e: Exception) {
                timeString
            }
        }
    }
}