package com.example.feature_location.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.core.api.GeocodeCandidate
import com.example.feature_location.databinding.ItemSearchResultBinding

class SearchResultsAdapter(
    private val onResultClick: (GeocodeCandidate) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private var results = mutableListOf<GeocodeCandidate>()

    fun updateResults(newResults: List<GeocodeCandidate>) {
        results.clear()
        results.addAll(newResults)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: GeocodeCandidate) {
            binding.tvLocationName.text = result.displayName
            binding.tvAddress.text = buildString {
                if (result.block.isNotEmpty()) append("${result.block} ")
                if (result.road.isNotEmpty()) append(result.road)
                if (result.building.isNotEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(result.building)
                }
            }
            binding.tvPostalCode.text = result.postalCode
            
            binding.root.setOnClickListener {
                onResultClick(result)
            }
        }
    }
}