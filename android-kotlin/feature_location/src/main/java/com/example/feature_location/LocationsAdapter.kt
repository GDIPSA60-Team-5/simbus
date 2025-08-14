package com.example.feature_location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.core.api.SavedLocation
import com.example.feature_location.databinding.ItemLocationBinding

class LocationsAdapter(
    private val onLocationClick: (SavedLocation) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    private var locations = mutableListOf<SavedLocation>()

    fun updateLocations(newLocations: List<SavedLocation>) {
        locations.clear()
        locations.addAll(newLocations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: SavedLocation) {
            binding.tvLocationName.text = location.locationName
            binding.tvCoordinates.text = "${location.latitude}, ${location.longitude}"
            
            binding.root.setOnClickListener {
                onLocationClick(location)
            }
        }
    }
}