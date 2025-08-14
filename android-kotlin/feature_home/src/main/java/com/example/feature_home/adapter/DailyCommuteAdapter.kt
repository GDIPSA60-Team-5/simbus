package com.example.feature_home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.core.api.CommutePlan
import com.example.feature_home.databinding.ItemDailyCommuteCardBinding

data class DayCommutes(
    val dayName: String,
    val commutes: List<CommutePlan>
)

class DailyCommuteAdapter(
    private var dayCommutes: List<DayCommutes> = emptyList()
) : RecyclerView.Adapter<DailyCommuteAdapter.DayCommuteViewHolder>() {

    fun updateDays(newDayCommutes: List<DayCommutes>) {
        dayCommutes = newDayCommutes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayCommuteViewHolder {
        val binding = ItemDailyCommuteCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayCommuteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayCommuteViewHolder, position: Int) {
        holder.bind(dayCommutes[position])
    }

    override fun getItemCount(): Int = dayCommutes.size

    class DayCommuteViewHolder(
        private val binding: ItemDailyCommuteCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val commutePlanAdapter = CommutePlanAdapter()

        init {
            binding.commutesRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = commutePlanAdapter
            }
        }

        fun bind(dayCommute: DayCommutes) {
            binding.dayTitle.text = dayCommute.dayName
            
            if (dayCommute.commutes.isEmpty()) {
                binding.commutesRecyclerView.visibility = View.GONE
                binding.noCommutesText.visibility = View.VISIBLE
            } else {
                binding.commutesRecyclerView.visibility = View.VISIBLE
                binding.noCommutesText.visibility = View.GONE
                commutePlanAdapter.updateCommutes(dayCommute.commutes)
            }
        }
    }
}