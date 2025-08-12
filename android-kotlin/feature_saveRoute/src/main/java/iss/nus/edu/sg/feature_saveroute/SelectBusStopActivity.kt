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
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.Data.NusBusStop
import iss.nus.edu.sg.feature_saveroute.Data.SgBusStop
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectBusStopActivity : AppCompatActivity() {

    @Inject
    lateinit var routeController: RouteController

    private lateinit var busStopAdapter: ArrayAdapter<String>
    private var currentType: String = "SG"
    private var currentSgStops: List<SgBusStop> = emptyList()
    private var currentNusStops: List<NusBusStop> = emptyList()

    private var filteredSgStops: List<SgBusStop> = emptyList()
    private var filteredNusStops: List<NusBusStop> = emptyList()


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

        lifecycleScope.launch {
            routeController.searchBusStops("").fold(
                onSuccess = { allStops ->
                    Log.d("SelectBusStop", "API Response received")
                    Log.d("SelectBusStop", "Total stops received: ${allStops.size}")

                    allStops.take(3).forEach { stop ->
                        Log.d(
                            "SelectBusStop",
                            "Stop: ${stop.name} (${stop.code}) - API: ${stop.sourceApi}"
                        )
                    }

                    BusStopCache.allBusStops = allStops

                    BusStopCache.sgBusStops = BusStopCache.getSgBusStopsFromUnified()
                    BusStopCache.nusBusStops = BusStopCache.getNusBusStopsFromUnified()

                    Log.d("SelectBusStop", "SG stops in cache: ${BusStopCache.sgBusStops?.size}")
                    Log.d("SelectBusStop", "NUS stops in cache: ${BusStopCache.nusBusStops?.size}")

                    displayBusStops(listView, busStopType)
                },
                onFailure = { error ->
                    Log.e("SelectBusStop", "API Call Failed", error)
                    Toast.makeText(
                        this@SelectBusStopActivity,
                        "Network error: ${error.message}", Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
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

    private fun displaySgBusStops(
        busStops: List<SgBusStop>,
        listView: ListView,
        busStopType: String
    ) {
        Log.d("SelectBusStop", "Setting up SG adapter with ${busStops.size} stops")

        if (busStops.isEmpty()) {
            Toast.makeText(this, "No SG bus stops found", Toast.LENGTH_SHORT).show()
            return
        }

        currentSgStops = busStops
        filteredSgStops = busStops

        val labels =
            filteredSgStops.map { "${it.description ?: "Unnamed Stop"} (${it.busStopCode ?: "NoCode"})" }


        busStopAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        listView.adapter = busStopAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = filteredSgStops[position]
            val selectedStopCode = selected.busStopCode
            Log.d("SelectBusStop", "Selected SG stop: $selectedStopCode")
            val resultIntent = Intent().apply {
                putExtra("BusStopCode", selectedStopCode)
                putExtra("BusStopType", busStopType)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun displayNusBusStops(
        busStops: List<NusBusStop>,
        listView: ListView,
        busStopType: String
    ) {

        if (busStops.isEmpty()) {
            Toast.makeText(this, "No NUS bus stops found", Toast.LENGTH_SHORT).show()
            return
        }

        currentNusStops = busStops
        filteredNusStops = busStops

        val labels = filteredNusStops.map { "${it.longName} (${it.name})" }

        busStopAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        listView.adapter = busStopAdapter


        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = filteredNusStops[position]
            val selectedStopName = selected.name
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
        if (currentType == "SG") {

            filteredSgStops = currentSgStops.filter {
                (it.description ?: "").contains(q, ignoreCase = true) ||
                        (it.busStopCode ?: "").contains(q, ignoreCase = true)
            }

            val labels = filteredSgStops.map {
                "${it.description ?: "Unnamed Stop"} (${it.busStopCode ?: "NoCode"})"
            }

            busStopAdapter.clear()
            busStopAdapter.addAll(labels)
            busStopAdapter.notifyDataSetChanged()
        } else {
            filteredNusStops = currentNusStops.filter {
                (it.longName ?: "").contains(q, ignoreCase = true) ||
                        (it.name ?: "").contains(q, ignoreCase = true)
            }
            val labels = filteredNusStops.map { "${it.longName} (${it.name})" }
            busStopAdapter.clear()
            busStopAdapter.addAll(labels)
            busStopAdapter.notifyDataSetChanged()
        }
    }
}