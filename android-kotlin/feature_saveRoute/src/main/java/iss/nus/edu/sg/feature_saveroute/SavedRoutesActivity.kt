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

class SavedRoutesActivity : AppCompatActivity() {
    private lateinit var binding: SavedRoutesBinding
    private lateinit var adapter: MyCustomAdapter
    private val routes = mutableListOf<Route>()

    private val editRouteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val updatedRoute = data?.getParcelableExtra<Route>("Edit_Route")
            val position = data?.getIntExtra("Position", -1)?:-1
            if (updatedRoute !=null && position>=0){
                updateRouteOnServer(updatedRoute, position)
                routes[position] = updatedRoute
                adapter.notifyDataSetChanged()
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
            val intent = Intent(this, AddEditRouteActivity::class.java).apply{
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

        RetrofitClient.api.updateRoute(deviceId, routeId, body)
            .enqueue(object : retrofit2.Callback<RouteMongo> {
                override fun onResponse(call: retrofit2.Call<RouteMongo>, res: retrofit2.Response<RouteMongo>) {
                    if (res.isSuccessful && res.body() != null) {
                        val serverRouteUi = res.body()!!.toUi()
                        routes[position] = serverRouteUi
                        adapter.notifyItemChanged(position)
                        Toast.makeText(this@SavedRoutesActivity, "Route updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SavedRoutesActivity, "Update failed: ${res.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<RouteMongo>, t: Throwable) {
                    Toast.makeText(this@SavedRoutesActivity, "Network: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun deleteRouteFromServer(position: Int) {
        val route = routes[position]
        val routeId = route.id ?: run {
            Toast.makeText(this, "Missing route id to delete", Toast.LENGTH_SHORT).show()
            return
        }
        val deviceId = DeviceIdUtil.getDeviceId(this)

        RetrofitClient.api.deleteRoute(deviceId, routeId)
            .enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, r: retrofit2.Response<Void>) {
                    if (r.isSuccessful) {
                        routes.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, routes.size - position)
                        binding.activeRoutesNumber.text = routes.size.toString()
                        Toast.makeText(this@SavedRoutesActivity, "Route deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SavedRoutesActivity, "Delete failed: ${r.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(this@SavedRoutesActivity, "Network: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun fetchSavedRoutes() {
        val deviceId = DeviceIdUtil.getDeviceId(this)

        RetrofitClient.api.getSavedRoutes(deviceId)
            .enqueue(object : retrofit2.Callback<List<RouteMongo>> {
                override fun onResponse(
                    call: retrofit2.Call<List<RouteMongo>>,
                    res: retrofit2.Response<List<RouteMongo>>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        routes.clear()
                        routes.addAll(res.body()!!.map { it.toUi() })
                        adapter.notifyDataSetChanged()
                        binding.activeRoutesNumber.text = routes.size.toString()
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<RouteMongo>>, t: Throwable) {
                    Toast.makeText(this@SavedRoutesActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}