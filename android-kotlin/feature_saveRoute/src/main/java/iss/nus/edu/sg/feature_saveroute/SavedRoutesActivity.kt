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
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.RouteMongo
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SavedRoutesActivity : AppCompatActivity() {
    private lateinit var binding: SavedRoutesBinding
    private lateinit var adapter: MyCustomAdapter
    private val routes = mutableListOf<Route>()

    @Inject
    lateinit var routeController: RouteController

    private val editRouteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val updatedRoute = data?.getParcelableExtra<Route>("Edit_Route")
            val position = data?.getIntExtra("Position", -1) ?: -1
            if (updatedRoute != null && position >= 0) {
                updateRouteOnServer(updatedRoute, position)
                routes[position] = updatedRoute
                adapter.notifyDataSetChanged()
            }
        }
    }

    private val addRouteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val newRoute = data?.getParcelableExtra<Route>("New_Route")
            if (newRoute != null) {
                routes.add(newRoute)
                adapter.notifyDataSetChanged()
                binding.activeRoutesNumber.text = routes.size.toString()
            }
        }
    }

    private fun confirmDelete(position: Int) {
        val route = routes[position]
        val title = "Delete route?"
        val message = "Remove \"${route.from} → ${route.to}\"?"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deleteRouteFromServer(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyCustomAdapter(
            routes,
            onEditClick = { route, position ->
                val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("Edit_Route", route)
                    putExtra("Position", position)
                }
                editRouteLauncher.launch(intent)
            },
            onDeleteClick = { position ->
                confirmDelete(position)
            }
        )
        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activeRoutesNumber.text = routes.size.toString()

        binding.addRouteButton.setOnClickListener {
            val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addRouteLauncher.launch(intent)
        }
        fetchSavedRoutes()
    }

    private fun updateRouteOnServer(updated: Route, position: Int) {
        val deviceId = DeviceIdUtil.getDeviceId(this)
        val routeId = updated.id ?: run {
            Toast.makeText(this, "Missing route id to update", Toast.LENGTH_SHORT).show()
            return
        }

        val body = updated.toRequest()

        lifecycleScope.launch {
            routeController.updateRoute(deviceId, routeId, body).fold(
                onSuccess = { serverRoute ->
                    val serverRouteUi = serverRoute.toUi()
                    routes[position] = serverRouteUi
                    adapter.notifyItemChanged(position)
                    Toast.makeText(this@SavedRoutesActivity, "Route updated", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedRoutesActivity, "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun deleteRouteFromServer(position: Int) {
        val route = routes[position]
        val routeId = route.id ?: run {
            Toast.makeText(this, "Missing route id to delete", Toast.LENGTH_SHORT).show()
            return
        }
        val deviceId = DeviceIdUtil.getDeviceId(this)

        // Use injected routeController instead of RetrofitClient
        lifecycleScope.launch {
            routeController.deleteRoute(deviceId, routeId).fold(
                onSuccess = {
                    routes.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, routes.size)
                    binding.activeRoutesNumber.text = routes.size.toString()
                    Toast.makeText(this@SavedRoutesActivity, "Route deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedRoutesActivity, "Delete failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun fetchSavedRoutes() {
        val deviceId = DeviceIdUtil.getDeviceId(this)

        lifecycleScope.launch {
            routeController.getSavedRoutes(deviceId).fold(
                onSuccess = { routeMongoList ->
                    routes.clear()
                    routes.addAll(routeMongoList.map { it.toUi() })
                    adapter.notifyDataSetChanged()
                    binding.activeRoutesNumber.text = routes.size.toString()
                },
                onFailure = { error ->
                    Toast.makeText(this@SavedRoutesActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}