package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlanMongo
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SavedCommutePlansActivity : AppCompatActivity() {
    private lateinit var binding: SavedRoutesBinding
    private lateinit var adapter: MyCustomAdapter
    private val commutePlans = mutableListOf<CommutePlan>()

    @Inject
    lateinit var commutePlanController: CommutePlanController

    private val editPlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val updatedPlan = data?.getParcelableExtra<CommutePlan>("Edit_CommutePlan")
            val position = data?.getIntExtra("Position", -1) ?: -1
            if (updatedPlan != null && position >= 0) {
                updatePlanOnServer(updatedPlan, position)
            }
        }
    }

    private val addPlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val newPlan = data?.getParcelableExtra<CommutePlan>("New_CommutePlan")
            if (newPlan != null) {
                commutePlans.add(newPlan)
                adapter.notifyDataSetChanged()
                binding.activeRoutesNumber.text = commutePlans.size.toString()
            }
        }
    }

    private fun confirmDelete(position: Int) {
        val plan = commutePlans[position]
        val label = plan.commutePlanName ?: "${plan.startLocationId} → ${plan.endLocationId}"
        val title = "Delete commute plan?"
        val message = "Remove \"$label\"?"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ -> deletePlanFromServer(position) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyCustomAdapter(
            commutePlans,
            onEditClick = { plan, position ->
                val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("Edit_CommutePlan", plan)
                    putExtra("Position", position)
                }
                editPlanLauncher.launch(intent)
            },
            onDeleteClick = { position -> confirmDelete(position) }
        )
        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activeRoutesNumber.text = commutePlans.size.toString()

        binding.addRouteButton.setOnClickListener {
            val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addPlanLauncher.launch(intent)
        }

        fetchSavedCommutePlans()
    }

    private fun updatePlanOnServer(updated: CommutePlan, position: Int) {
        val planId = updated.id ?: run {
            Toast.makeText(this, "Missing commute plan id to update", Toast.LENGTH_SHORT).show()
            return
        }
        val body = updated.toRequest()

        lifecycleScope.launch {
            commutePlanController.updateCommutePlan(planId, body).fold(
                onSuccess = { serverPlan ->
                    val serverPlanUi = serverPlan.toUi()
                    commutePlans[position] = serverPlanUi
                    adapter.notifyItemChanged(position)
                    Toast.makeText(this@SavedCommutePlansActivity, "Plan updated", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedCommutePlansActivity, "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun deletePlanFromServer(position: Int) {
        val plan = commutePlans[position]
        val planId = plan.id ?: run {
            Toast.makeText(this, "Missing commute plan id to delete", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            commutePlanController.deleteCommutePlan(planId).fold(
                onSuccess = {
                    commutePlans.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    if (position < commutePlans.size) {
                        adapter.notifyItemRangeChanged(position, commutePlans.size - position)
                    }
                    binding.activeRoutesNumber.text = commutePlans.size.toString()
                    Toast.makeText(this@SavedCommutePlansActivity, "Plan deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedCommutePlansActivity, "Delete failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun fetchSavedCommutePlans() {
        lifecycleScope.launch {
            commutePlanController.getSavedCommutePlans().fold(
                onSuccess = { plansMongo: List<CommutePlanMongo> ->
                    // Sort by name for consistent ordering
                    val sortedPlans = plansMongo
                        .sortedWith(compareBy<CommutePlanMongo> { it.commutePlanName }
                            .thenBy { it.id })
                        .map { it.toUi() }

                    commutePlans.clear()
                    commutePlans.addAll(sortedPlans)
                    adapter.notifyDataSetChanged()
                    binding.activeRoutesNumber.text = commutePlans.size.toString()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedCommutePlansActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}