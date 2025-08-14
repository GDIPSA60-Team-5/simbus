package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.RouteMongo
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import com.example.core.api.CommuteApi
import com.example.core.api.CommutePlan
import com.example.core.api.LocationApi
import com.example.core.api.SavedLocation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedRoutesActivity : AppCompatActivity() {
    private lateinit var binding: SavedRoutesBinding
    private lateinit var adapter: CommutePlansAdapter
    private val commutePlans = mutableListOf<CommutePlan>()
    private var savedLocations = listOf<SavedLocation>()
    
    @Inject
    lateinit var commuteApi: CommuteApi
    
    @Inject
    lateinit var locationApi: LocationApi

    private val editCommutePlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val updatedCommutePlan = data?.getParcelableExtra<CommutePlan>("Updated_CommutePlan")
            if (updatedCommutePlan != null) {
                fetchCommutePlans() // Refresh the list
            }
        }
    }
    private val addCommutePlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val newCommutePlan = data?.getParcelableExtra<CommutePlan>("New_CommutePlan")
            if (newCommutePlan != null) {
                fetchCommutePlans() // Refresh the list
            }
        }
    }

    private fun confirmDelete(position: Int) {
        val commutePlan = commutePlans[position]
        val title = "Delete commute plan?"
        val message = "Remove \"${commutePlan.commutePlanName}\"?"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deleteCommutePlanFromServer(commutePlan.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CommutePlansAdapter(
            commutePlans,
            savedLocations,
            onEditClick = { commutePlan, position ->
                val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("commutePlanId", commutePlan.id)
                    putExtra("Position", position)
                }
                editCommutePlanLauncher.launch(intent)
            },
            onDeleteClick = { position ->
                confirmDelete(position)
            }
        )
        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activeRoutesNumber.text = commutePlans.size.toString()



        binding.addRouteButton.setOnClickListener {
            val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addCommutePlanLauncher.launch(intent)
        }
        
        loadData()
    }

    private fun deleteCommutePlanFromServer(commutePlanId: String) {
        lifecycleScope.launch {
            try {
                val response = commuteApi.deleteCommutePlan(commutePlanId)
                if (response.isSuccessful) {
                    fetchCommutePlans() // Refresh the list
                    Toast.makeText(this@SavedRoutesActivity, "Commute plan deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SavedRoutesActivity, "Delete failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SavedRoutesActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                // Load locations first
                val locationsResponse = locationApi.getUserLocations()
                if (locationsResponse.isSuccessful) {
                    savedLocations = locationsResponse.body() ?: emptyList()
                    adapter.updateLocations(savedLocations)
                }
                
                // Then load commute plans
                fetchCommutePlans()
            } catch (e: Exception) {
                Toast.makeText(this@SavedRoutesActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchCommutePlans() {
        lifecycleScope.launch {
            try {
                val response = commuteApi.getMyCommutes()
                if (response.isSuccessful && response.body() != null) {
                    commutePlans.clear()
                    commutePlans.addAll(response.body()!!)
                    adapter.updateCommutePlans(commutePlans)
                    binding.activeRoutesNumber.text = commutePlans.size.toString()
                } else {
                    Toast.makeText(this@SavedRoutesActivity, "Failed to load commute plans", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SavedRoutesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}