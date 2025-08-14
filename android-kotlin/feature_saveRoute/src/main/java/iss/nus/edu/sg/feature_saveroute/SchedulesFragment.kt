package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import com.example.core.api.CommuteApi
import com.example.core.api.CommutePlan
import com.example.core.api.LocationApi
import com.example.core.api.SavedLocation
import iss.nus.edu.sg.feature_saveroute.databinding.FragmentSchedulesBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SchedulesFragment : Fragment() {

    private var _binding: FragmentSchedulesBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val TAG = "SchedulesFragment"
    }

    private lateinit var adapter: CommutePlansAdapter
    private val commutePlans = mutableListOf<CommutePlan>()
    private var savedLocations = listOf<SavedLocation>()

    @Inject
    lateinit var commuteApi: CommuteApi

    @Inject
    lateinit var locationApi: LocationApi

    private val editCommutePlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedCommutePlan = result.data?.getParcelableExtra<CommutePlan>("Updated_CommutePlan")
            if (updatedCommutePlan != null) {
                fetchCommutePlans()
            }
        }
    }

    private val addCommutePlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newCommutePlan = result.data?.getParcelableExtra<CommutePlan>("New_CommutePlan")
            if (newCommutePlan != null) {
                fetchCommutePlans()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CommutePlansAdapter(
            commutePlans,
            savedLocations,
            onEditClick = { commutePlan, position ->
                val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
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

        binding.routesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.routesRecyclerView.adapter = adapter
        binding.activeRoutesNumber.text = commutePlans.size.toString()

        binding.addRouteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addCommutePlanLauncher.launch(intent)
        }

        loadData()
    }

    private fun confirmDelete(position: Int) {
        val commutePlan = commutePlans[position]
        val title = "Delete commute plan?"
        val message = "Remove \"${commutePlan.commutePlanName}\"?"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deleteCommutePlanFromServer(commutePlan.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCommutePlanFromServer(commutePlanId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = commuteApi.deleteCommutePlan(commutePlanId)
                if (response.isSuccessful) {
                    fetchCommutePlans()
                    Toast.makeText(requireContext(), "Commute plan deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Delete failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadData() {
        Log.d(TAG, "loadData() called")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading locations...")
                val locationsResponse = locationApi.getUserLocations()
                Log.d(TAG, "Locations response: ${locationsResponse.code()}")
                if (locationsResponse.isSuccessful) {
                    savedLocations = locationsResponse.body() ?: emptyList()
                    Log.d(TAG, "Loaded ${savedLocations.size} locations")
                    adapter.updateLocations(savedLocations)
                }
                Log.d(TAG, "Fetching commute plans...")
                fetchCommutePlans()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                Toast.makeText(requireContext(), "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchCommutePlans() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling commuteApi.getMyCommutes()...")
                val response = commuteApi.getMyCommutes()
                Log.d(TAG, "Response: ${response.code()} - ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val plans = response.body()!!
                    Log.d(TAG, "Received ${plans.size} commute plans")
                    
                    commutePlans.clear()
                    commutePlans.addAll(plans)
                    // Don't call adapter.updateCommutePlans() since we share the same list reference
                    adapter.notifyDataSetChanged()
                    binding.activeRoutesNumber.text = commutePlans.size.toString()
                    
                    Log.d(TAG, "Updated UI - Active routes: ${commutePlans.size}")
                    
                    // Log each plan for debugging
                    plans.forEachIndexed { index, plan ->
                        Log.d(TAG, "Plan $index: ${plan.commutePlanName} - ${plan.notifyAt} - Recurrence: ${plan.recurrence}")
                    }
                } else {
                    Log.e(TAG, "Failed to load commute plans: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load commute plans: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching commute plans", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
