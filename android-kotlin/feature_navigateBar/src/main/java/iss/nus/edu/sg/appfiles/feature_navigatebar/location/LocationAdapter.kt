package iss.nus.edu.sg.appfiles.feature_navigatebar.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.SavedLocationListBinding
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo

class LocationAdapter(
    private val items: MutableList<SavedLocationMongo>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<LocationAdapter.VH>() {

    inner class VH(val b : SavedLocationListBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = SavedLocationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        b.locationNumber.text = "Location ${position + 1}"
        b.locationName.text = "Name: ${item.name}"
        b.locationPostal.text = "Postal Code: ${item.postalCode}"

        b.deleteLocationButton.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onDeleteClick(pos)
        }
    }
}