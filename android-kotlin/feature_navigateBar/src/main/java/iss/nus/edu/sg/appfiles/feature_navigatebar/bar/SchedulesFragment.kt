package iss.nus.edu.sg.appfiles.feature_navigatebar.bar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import iss.nus.edu.sg.feature_saveroute.AddEditRouteActivity
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.RouteMongo
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.Data.toUi
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import iss.nus.edu.sg.feature_saveroute.MyCustomAdapter
import iss.nus.edu.sg.feature_saveroute.RetrofitClient
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SchedulesFragment : Fragment() {

    private var _binding: SavedRoutesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyCustomAdapter
    private val routes = mutableListOf<Route>()

    private val editRouteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedRoute = data?.getParcelableExtra<Route>("Edit_Route")
                val position = data?.getIntExtra("Position", -1) ?: -1
                if (updatedRoute != null && position >= 0) {
                    updateRouteOnServer(updatedRoute, position)
                    routes[position] = updatedRoute
                    adapter.notifyItemChanged(position)
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

        RetrofitClient.api.updateRoute(deviceId, routeId, body)
            .enqueue(object : Callback<RouteMongo> {
                override fun onResponse(call: Call<RouteMongo>, res: Response<RouteMongo>) {
                    if (!isAdded) return
                    if (res.isSuccessful && res.body() != null) {
                        val serverRouteUi = res.body()!!.toUi()
                        routes[position] = serverRouteUi
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Route updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Update failed: ${res.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RouteMongo>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Network: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun deleteRouteFromServer(position: Int) {
        val route = routes[position]
        val routeId = route.id ?: run {
            Toast.makeText(requireContext(), "Missing route id to delete", Toast.LENGTH_SHORT).show()
            return
        }
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())

        RetrofitClient.api.deleteRoute(deviceId, routeId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, r: Response<Void>) {
                    if (!isAdded) return
                    if (r.isSuccessful) {
                        routes.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        if (position < routes.size) {
                            adapter.notifyItemRangeChanged(position, routes.size - position)
                        }
                        binding.activeRoutesNumber.text = routes.size.toString()
                        Toast.makeText(requireContext(), "Route deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Delete failed: ${r.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Network: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun fetchSavedRoutes() {
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())

        RetrofitClient.api.getSavedRoutes(deviceId)
            .enqueue(object : Callback<List<RouteMongo>> {
                override fun onResponse(
                    call: Call<List<RouteMongo>>,
                    res: Response<List<RouteMongo>>
                ) {
                    if (!isAdded) return
                    if (res.isSuccessful && res.body() != null) {
                        routes.clear()
                        routes.addAll(res.body()!!.map { it.toUi() })
                        adapter.notifyDataSetChanged()
                        binding.activeRoutesNumber.text = routes.size.toString()
                    } else {
                        Toast.makeText(requireContext(), "Failed to load routes", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<RouteMongo>>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}