package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.feature_saveroute.Data.NusBusServiceAtStop
import iss.nus.edu.sg.feature_saveroute.Data.SgBusServiceAtStop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectBusServiceActivity : AppCompatActivity() {
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
        RetrofitClient.api.getSgBusServices(busStopCode)
            .enqueue(object : Callback<List<SgBusServiceAtStop>> {
                override fun onResponse(
                    call: Call<List<SgBusServiceAtStop>>,
                    response: Response<List<SgBusServiceAtStop>>
                ) {
                    if (response.isSuccessful) {
                        val services = response.body() ?: emptyList()
                        if (services.isEmpty()) {
                            Toast.makeText(this@SelectBusServiceActivity,
                                "No SG services found", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val serviceNumbers = services.mapNotNull { it.serviceNo }
                        setAdapter(listView, serviceNumbers)
                    } else {
                        Toast.makeText(this@SelectBusServiceActivity,
                            "SG Fetch failed: ${response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<SgBusServiceAtStop>>, t: Throwable) {
                    Toast.makeText(this@SelectBusServiceActivity,
                        "Error fetching SG services: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchNusBusServices(busStopName: String, listView: ListView) {
        RetrofitClient.api.getNusBusServices(busStopName)
            .enqueue(object : Callback<List<NusBusServiceAtStop>> {
                override fun onResponse(
                    call: Call<List<NusBusServiceAtStop>>,
                    response: Response<List<NusBusServiceAtStop>>
                ) {
                    if (response.isSuccessful) {
                        val services = response.body() ?: emptyList()
                        if (services.isEmpty()) {
                            Toast.makeText(this@SelectBusServiceActivity,
                                "No NUS services found", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val serviceNames = services.mapNotNull { it.name }
                        Log.d("BusService", ">>> NUS services found: $serviceNames")
                        setAdapter(listView, serviceNames)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("BusService", ">>> NUS Fetch failed: $errorBody")
                        Toast.makeText(this@SelectBusServiceActivity,
                            "NUS Fetch failed: $errorBody",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<NusBusServiceAtStop>>, t: Throwable) {
                    Log.e("BusService", ">>> Error fetching NUS services", t)
                    Toast.makeText(this@SelectBusServiceActivity,
                        "Error fetching NUS services: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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