package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.feature_saveroute.Data.NusBusStop
import iss.nus.edu.sg.feature_saveroute.Data.SgBusStop
import iss.nus.edu.sg.feature_saveroute.Data.UnifiedBusStop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectBusStopActivity : AppCompatActivity() {

    private lateinit var busStopAdapter: ArrayAdapter<String>
    private var currentType: String = "SG"
    private var currentSgStops: List<SgBusStop> = emptyList()
    private var currentNusStops: List<NusBusStop> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectbusstop)

        val busStopType = intent.getStringExtra("BusStopType") ?: "SG"
        val busStopListView = findViewById<ListView>(R.id.busStopListView)
        val searchView = findViewById<SearchView>(R.id.searchView)

        Log.d("SelectBusStop", "=== Starting SelectBusStopActivity ===")
        Log.d("SelectBusStop", "Bus stop type: $busStopType")
        Log.d("SelectBusStop", "Cache status - allBusStops: ${BusStopCache.allBusStops?.size}")

        if (BusStopCache.allBusStops == null) {
            Log.d("SelectBusStop", "Cache is null, fetching from API...")
            fetchAllBusStops(busStopListView, busStopType)
        } else {
            Log.d("SelectBusStop", "Using cached data...")
            displayBusStops(busStopListView, busStopType)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilter(query.orEmpty())
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilter(newText.orEmpty())
                return true
            }
        })

    }

    private fun fetchAllBusStops(listView: ListView, busStopType: String) {
        Log.d("SelectBusStop", "Making API call to search bus stops...")

        Toast.makeText(this, "Loading bus stops...", Toast.LENGTH_SHORT).show()

        RetrofitClient.api.searchBusStops("").enqueue(object : Callback<List<UnifiedBusStop>> {
            override fun onResponse(call: Call<List<UnifiedBusStop>>, response: Response<List<UnifiedBusStop>>) {
                Log.d("SelectBusStop", "API Response received")
                Log.d("SelectBusStop", "Response code: ${response.code()}")
                Log.d("SelectBusStop", "Response success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val allStops = response.body() ?: emptyList()
                    Log.d("SelectBusStop", "Total stops received: ${allStops.size}")

                    allStops.take(3).forEach { stop ->
                        Log.d("SelectBusStop", "Stop: ${stop.name} (${stop.code}) - API: ${stop.sourceApi}")
                    }

                    BusStopCache.allBusStops = allStops

                    BusStopCache.sgBusStops = BusStopCache.getSgBusStopsFromUnified()
                    BusStopCache.nusBusStops = BusStopCache.getNusBusStopsFromUnified()

                    Log.d("SelectBusStop", "SG stops in cache: ${BusStopCache.sgBusStops?.size}")
                    Log.d("SelectBusStop", "NUS stops in cache: ${BusStopCache.nusBusStops?.size}")

                    displayBusStops(listView, busStopType)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SelectBusStop", "API Error: $errorBody")
                    Toast.makeText(this@SelectBusStopActivity,
                        "Failed to load bus stops: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<UnifiedBusStop>>, t: Throwable) {
                Log.e("SelectBusStop", "API Call Failed", t)
                Toast.makeText(this@SelectBusStopActivity,
                    "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayBusStops(listView: ListView, busStopType: String) {
        Log.d("SelectBusStop", "Displaying bus stops for type: $busStopType")

        if (busStopType == "SG") {
            val sgStops = BusStopCache.getSgBusStopsFromUnified()
            Log.d("SelectBusStop", "Displaying ${sgStops.size} SG stops")
            displaySgBusStops(sgStops, listView, busStopType)
        } else {
            val nusStops = BusStopCache.getNusBusStopsFromUnified()
            Log.d("SelectBusStop", "Displaying ${nusStops.size} NUS stops")
            displayNusBusStops(nusStops, listView, busStopType)
        }

        currentType = busStopType
    }

    private fun displaySgBusStops(busStops: List<SgBusStop>, listView: ListView, busStopType: String) {
        Log.d("SelectBusStop", "Setting up SG adapter with ${busStops.size} stops")

        if (busStops.isEmpty()) {
            Toast.makeText(this, "No SG bus stops found", Toast.LENGTH_SHORT).show()
            return
        }

        val names = busStops.map { "${it.description ?: "Unnamed Stop"} (${it.busStopCode ?: "NoCode"})" }

        currentSgStops = busStops
        busStopAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            busStops.map { "${it.description ?: "Unnamed Stop"} (${it.busStopCode ?: "NoCode"})" }
        )
        listView.adapter = busStopAdapter

        Log.d("SelectBusStop", "SG Adapter set with ${names.size} items")

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedStopCode = busStops[position].busStopCode
            Log.d("SelectBusStop", "Selected SG stop: $selectedStopCode")
            val resultIntent = Intent().apply {
                putExtra("BusStopCode", selectedStopCode)
                putExtra("BusStopType", busStopType)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


    }

    private fun displayNusBusStops(busStops: List<NusBusStop>, listView: ListView, busStopType: String) {
        Log.d("SelectBusStop", "Setting up NUS adapter with ${busStops.size} stops")

        if (busStops.isEmpty()) {
            Toast.makeText(this, "No NUS bus stops found", Toast.LENGTH_SHORT).show()
            return
        }

        val displayItems = busStops.map { "${it.longName} (${it.name})" }
        currentNusStops = busStops
        busStopAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            busStops.map { "${it.longName} (${it.name})" }
        )
        listView.adapter = busStopAdapter

        Log.d("SelectBusStop", "NUS Adapter set with ${displayItems.size} items")

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedStopName = busStops[position].name
            Log.d("SelectBusStop", "Selected NUS stop: $selectedStopName")
            val resultIntent = Intent().apply {
                putExtra("BusStopCode", selectedStopName)
                putExtra("BusStopType", busStopType)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun applyFilter(q: String) {
        if (!::busStopAdapter.isInitialized) return

        val filtered: List<String> = if (currentType == "SG") {
            currentSgStops
                .filter {
                    (it.description ?: "").contains(q, ignoreCase = true) ||
                            (it.busStopCode ?: "").contains(q, ignoreCase = true)
                }
                .map { "${it.description ?: "Unnamed Stop"} (${it.busStopCode ?: "NoCode"})" }
        } else {
            currentNusStops
                .filter {
                    (it.longName ?: "").contains(q, ignoreCase = true) ||
                            (it.name ?: "").contains(q, ignoreCase = true)
                }
                .map { "${it.longName} (${it.name})" }
        }

        busStopAdapter.clear()
        busStopAdapter.addAll(filtered)
        busStopAdapter.notifyDataSetChanged()
    }
}