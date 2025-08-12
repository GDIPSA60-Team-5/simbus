package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectBusServiceActivity : AppCompatActivity() {

    @Inject
    lateinit var routeController: RouteController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectbusservice)

        val busStopCode = intent.getStringExtra("BusStopCode") ?: ""
        val listView = findViewById<ListView>(R.id.busServiceListView)

        if (busStopCode.isEmpty()) {
            Toast.makeText(this, "No bus stop selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (busStopCode.matches(Regex("\\d{5}"))) {
            Log.d("BusService", ">>> Detected SG stop: $busStopCode")
            fetchSgBusServices(busStopCode, listView)
        } else {
            Log.d("BusService", ">>> Detected NUS stop: $busStopCode")
            fetchNusBusServices(busStopCode, listView)
        }
    }

    private fun fetchSgBusServices(busStopCode: String, listView: ListView) {
        lifecycleScope.launch {
            routeController.getSgBusServices(busStopCode).fold(
                onSuccess = { services ->
                    if (services.isEmpty()) {
                        Toast.makeText(this@SelectBusServiceActivity,
                            "No SG services found", Toast.LENGTH_SHORT).show()
                        return@fold
                    }

                    val serviceNumbers = services.mapNotNull { it.serviceNo }
                    setAdapter(listView, serviceNumbers)
                },
                onFailure = { error ->
                    Toast.makeText(this@SelectBusServiceActivity,
                        "SG Fetch failed: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                    Log.e("BusService", "Error fetching SG services", error)
                }
            )
        }
    }

    private fun fetchNusBusServices(busStopName: String, listView: ListView) {
        lifecycleScope.launch {
            routeController.getNusBusServices(busStopName).fold(
                onSuccess = { services ->
                    if (services.isEmpty()) {
                        Toast.makeText(this@SelectBusServiceActivity,
                            "No NUS services found", Toast.LENGTH_SHORT).show()
                        return@fold
                    }

                    val serviceNames = services.mapNotNull { it.name }
                    Log.d("BusService", ">>> NUS services found: $serviceNames")
                    setAdapter(listView, serviceNames)
                },
                onFailure = { error ->
                    Log.e("BusService", ">>> Error fetching NUS services", error)
                    Toast.makeText(this@SelectBusServiceActivity,
                        "Error fetching NUS services: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setAdapter(listView: ListView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedService = items[position]
            val resultIntent = Intent().apply {
                putExtra("SelectedBusService", selectedService)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}