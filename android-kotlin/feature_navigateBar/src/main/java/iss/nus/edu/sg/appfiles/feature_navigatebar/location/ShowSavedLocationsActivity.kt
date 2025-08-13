package iss.nus.edu.sg.appfiles.feature_navigatebar.location

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.ShowSavedLocationsBinding
import iss.nus.edu.sg.feature_saveroute.CreateSavedLocationActivity
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo
import iss.nus.edu.sg.feature_saveroute.SavedLocationsController
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowSavedLocationsActivity : AppCompatActivity() {
    private lateinit var binding: ShowSavedLocationsBinding
    private lateinit var adapter: LocationAdapter
    private val savedLocations = mutableListOf<SavedLocationMongo>()

    @Inject lateinit var savedLocationsController: SavedLocationsController

    private val addLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchLocations()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShowSavedLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.LocationPageTitle.text = "Saved Locations"
        binding.manageLocations.text = "Manage your saved locations here"
        binding.activeLocations.text = "Saved locations:"
        binding.activeLocationNumber.text = "0"

        adapter = LocationAdapter(savedLocations,
            onDeleteClick = { position -> confirmDelete(position) }
        )

        binding.LocationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.LocationRecyclerView.adapter = adapter

        binding.addLocationButton.setOnClickListener {
            val intent = Intent(this, CreateSavedLocationActivity::class.java)
            addLocationLauncher.launch(intent)
        }

        fetchLocations()
    }

    private fun confirmDelete(position: Int) {
        val label = savedLocations[position].name ?: "(no name)"
        AlertDialog.Builder(this)
            .setTitle("Delete location?")
            .setMessage("Remove \"$label\"?")
            .setPositiveButton("Delete") { _, _ -> deleteLocation(position) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteLocation(position: Int) {
        val id = savedLocations[position].id ?: return
        lifecycleScope.launch {
            savedLocationsController.deleteLocation(id).fold(
                onSuccess = {
                    savedLocations.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    if (position < savedLocations.size) {
                        adapter.notifyItemRangeChanged(position, savedLocations.size - position)
                    }
                    binding.activeLocationNumber.text = savedLocations.size.toString()
                    Toast.makeText(this@ShowSavedLocationsActivity, "Location deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(this@ShowSavedLocationsActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun fetchLocations() {
        lifecycleScope.launch {
            savedLocationsController.getStoredLocations().fold(
                onSuccess = { list ->
                    val sorted = list.sortedWith(compareBy<SavedLocationMongo> { it.name }.thenBy { it.id })
                    savedLocations.clear()
                    savedLocations.addAll(sorted)
                    adapter.notifyDataSetChanged()
                    binding.activeLocationNumber.text = savedLocations.size.toString()
                },
                onFailure = { e ->
                    Toast.makeText(this@ShowSavedLocationsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}