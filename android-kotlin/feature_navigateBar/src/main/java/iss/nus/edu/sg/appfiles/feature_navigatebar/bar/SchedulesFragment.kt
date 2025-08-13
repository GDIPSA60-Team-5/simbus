package iss.nus.edu.sg.appfiles.feature_navigatebar.bar

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.AddEditRouteActivity
import iss.nus.edu.sg.feature_saveroute.CommutePlanController
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.MyCustomAdapter
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SchedulesFragment : Fragment() {

    private var _binding: SavedRoutesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyCustomAdapter
    private val plans = mutableListOf<CommutePlan>()

    @Inject
    lateinit var commutePlanController: CommutePlanController

    private val editPlanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedPlan = data?.getParcelableExtra<CommutePlan>("Edit_CommutePlan")
                val position = data?.getIntExtra("Position", -1) ?: -1
                if (updatedPlan != null && position >= 0) {
                    updatePlanOnServer(updatedPlan, position)
                }
            }
        }

    private val addPlanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val newPlan = data?.getParcelableExtra<CommutePlan>("New_CommutePlan")
                if (newPlan != null) {
                    plans.add(newPlan)
                    adapter.notifyItemInserted(plans.lastIndex)
                    binding.activeRoutesNumber.text = plans.size.toString()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SavedRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MyCustomAdapter(
            plans,
            onEditClick = { plan, position ->
                val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("Edit_CommutePlan", plan)
                    putExtra("Position", position)
                }
                editPlanLauncher.launch(intent)
            },
            onDeleteClick = { position ->
                confirmDelete(position)
            }
        )

        binding.routesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.routesRecyclerView.adapter = adapter
        binding.activeRoutesNumber.text = plans.size.toString()

        binding.addRouteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addPlanLauncher.launch(intent)
        }

        fetchSavedCommutePlans()
    }

    private fun confirmDelete(position: Int) {
        val plan = plans[position]
        val label = plan.commutePlanName ?: "${plan.startLocationId} → ${plan.endLocationId}"
        val title = "Delete commute plan?"
        val message = "Remove \"$label\"?"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deletePlanFromServer(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePlanOnServer(updated: CommutePlan, position: Int) {
        val planId = updated.id ?: run {
            Toast.makeText(requireContext(), "Missing commute plan id to update", Toast.LENGTH_SHORT).show()
            return
        }

        val body = updated.toRequest()

        viewLifecycleOwner.lifecycleScope.launch {
            commutePlanController.updateCommutePlan(planId, body).fold(
                onSuccess = { serverPlan ->
                    if (!isAdded) return@fold
                    val serverUi = serverPlan.toUi()
                    plans[position] = serverUi
                    adapter.notifyItemChanged(position)
                    Toast.makeText(requireContext(), "Plan updated", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    if (!isAdded) return@fold
                    Toast.makeText(requireContext(), "Update failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun deletePlanFromServer(position: Int) {
        val plan = plans[position]
        val planId = plan.id ?: run {
            Toast.makeText(requireContext(), "Missing commute plan id to delete", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            commutePlanController.deleteCommutePlan(planId).fold(
                onSuccess = {
                    if (!isAdded) return@fold
                    plans.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    if (position < plans.size) {
                        adapter.notifyItemRangeChanged(position, plans.size - position)
                    }
                    binding.activeRoutesNumber.text = plans.size.toString()
                    Toast.makeText(requireContext(), "Plan deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    if (!isAdded) return@fold
                    Toast.makeText(requireContext(), "Delete failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun fetchSavedCommutePlans() {
        viewLifecycleOwner.lifecycleScope.launch {
            commutePlanController.getSavedCommutePlans().fold(
                onSuccess = { mongoList ->
                    if (!isAdded) return@fold
                    plans.clear()
                    plans.addAll(mongoList.map { it.toUi() })
                    adapter.notifyDataSetChanged()
                    binding.activeRoutesNumber.text = plans.size.toString()
                },
                onFailure = { exception ->
                    if (!isAdded) return@fold
                    Toast.makeText(requireContext(), "Failed to load plans: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}