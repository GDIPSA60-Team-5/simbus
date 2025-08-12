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
import iss.nus.edu.sg.feature_saveroute.AddEditRouteActivity
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import iss.nus.edu.sg.feature_saveroute.MyCustomAdapter
import iss.nus.edu.sg.feature_saveroute.RouteController
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SchedulesFragment : Fragment() {

    private var _binding: SavedRoutesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyCustomAdapter
    private val routes = mutableListOf<Route>()

    @Inject
    lateinit var routeController: RouteController

    private val editRouteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedRoute = data?.getParcelableExtra<Route>("Edit_Route")
                val position = data?.getIntExtra("Position", -1) ?: -1
                if (updatedRoute != null && position >= 0) {
                    updateRouteOnServer(updatedRoute, position)
                }
            }
        }

    private val addRouteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val newRoute = data?.getParcelableExtra<Route>("New_Route")
                if (newRoute != null) {
                    routes.add(newRoute)
                    adapter.notifyItemInserted(routes.lastIndex)
                    binding.activeRoutesNumber.text = routes.size.toString()
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
            routes,
            onEditClick = { route, position ->
                val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
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

        binding.routesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.routesRecyclerView.adapter = adapter
        binding.activeRoutesNumber.text = routes.size.toString()

        binding.addRouteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", false)
            }
            addRouteLauncher.launch(intent)
        }

        fetchSavedRoutes()
    }

    private fun confirmDelete(position: Int) {
        val route = routes[position]
        val title = "Delete route?"
        val message = "Remove \"${route.from} → ${route.to}\"?"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deleteRouteFromServer(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRouteOnServer(updated: Route, position: Int) {
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())
        val routeId = updated.id ?: run {
            Toast.makeText(requireContext(), "Missing route id to update", Toast.LENGTH_SHORT).show()
            return
        }

        val body = updated.toRequest()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = routeController.updateRoute(deviceId, routeId, body)

                result.fold(
                    onSuccess = { routeMongo ->
                        if (!isAdded) return@fold
                        val serverRouteUi = routeMongo.toUi()
                        routes[position] = serverRouteUi
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Route updated", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        if (!isAdded) return@fold
                        Toast.makeText(requireContext(), "Update failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                if (!isAdded) return@launch
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteRouteFromServer(position: Int) {
        val route = routes[position]
        val routeId = route.id ?: run {
            Toast.makeText(requireContext(), "Missing route id to delete", Toast.LENGTH_SHORT).show()
            return
        }
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = routeController.deleteRoute(deviceId, routeId)

                result.fold(
                    onSuccess = {
                        if (!isAdded) return@fold
                        routes.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        if (position < routes.size) {
                            adapter.notifyItemRangeChanged(position, routes.size - position)
                        }
                        binding.activeRoutesNumber.text = routes.size.toString()
                        Toast.makeText(requireContext(), "Route deleted", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        if (!isAdded) return@fold
                        Toast.makeText(requireContext(), "Delete failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                if (!isAdded) return@launch
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchSavedRoutes() {
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = routeController.getSavedRoutes(deviceId)

                result.fold(
                    onSuccess = { routeMongoList ->
                        if (!isAdded) return@fold
                        routes.clear()
                        routes.addAll(routeMongoList.map { it.toUi() })
                        adapter.notifyDataSetChanged()
                        binding.activeRoutesNumber.text = routes.size.toString()
                    },
                    onFailure = { exception ->
                        if (!isAdded) return@fold
                        Toast.makeText(requireContext(), "Failed to load routes: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                if (!isAdded) return@launch
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}