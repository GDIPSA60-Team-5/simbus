package com.example.feature_location.location

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.core.api.GeocodeCandidate
import com.example.core.api.CreateLocationRequest
import com.example.core.api.LocationApi
import android.util.Log
import com.example.core.permission.LocationPermissionManager
import com.example.feature_location.databinding.ActivityAddLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var locationPermissionManager: LocationPermissionManager
    private var isMapReady = false
    private var currentMarker: Marker? = null
    private var selectedLocation: LatLng? = null

    @Inject
    lateinit var locationApi: LocationApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMap()
        setupRecyclerView()
        setupSearchView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Add Location"
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(binding.mapFragment.id) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        
        // Set default location to Singapore
        val singapore = LatLng(1.3521, 103.8198)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 11f))
        
        // Allow user to place marker by tapping on map
        googleMap.setOnMapClickListener { latLng ->
            setLocationMarker(latLng)
        }
    }

    private fun setupRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter { candidate ->
            // When search result is clicked, place marker on map
            val latLng = LatLng(
                candidate.latitude.toDouble(),
                candidate.longitude.toDouble()
            )
            setLocationMarker(latLng)
            
            // Auto-fill the location name field
            binding.etLocationName.setText(candidate.displayName)
            
            // Hide search results
            binding.cardSearchResults.visibility = View.GONE
            binding.recyclerViewSearchResults.visibility = View.GONE
        }
        
        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(this@AddLocationActivity)
            adapter = searchResultsAdapter
        }
    }

    private fun setupSearchView() {
        binding.etSearchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                if (!query.isNullOrEmpty() && query.length >= 3) {
                    searchLocations(query)
                } else {
                    binding.cardSearchResults.visibility = View.GONE
                    binding.recyclerViewSearchResults.visibility = View.GONE
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveLocation.setOnClickListener {
            saveLocation()
        }
        
        binding.btnUseMapCenter.setOnClickListener {
            useMapCenter()
        }
        
        // Test button to debug the API
        binding.etSearchLocation.setOnLongClickListener {
            testAPI()
            true
        }
    }

    private fun searchLocations(query: String) {
        binding.progressBarSearch.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = locationApi.searchLocations(query)
                android.util.Log.d("AddLocation", "Response code: ${response.code()}")
                android.util.Log.d("AddLocation", "Response success: ${response.isSuccessful}")
                android.util.Log.d("AddLocation", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val geocodeResponse = response.body()!!
                    android.util.Log.d("AddLocation", "Found: ${geocodeResponse.found} results")
                    android.util.Log.d("AddLocation", "Results: ${geocodeResponse.results}")
                    
                    val results = geocodeResponse.results
                    if (results.isNotEmpty()) {
                        searchResultsAdapter.updateResults(results)
                        binding.cardSearchResults.visibility = View.VISIBLE
                        binding.recyclerViewSearchResults.visibility = View.VISIBLE
                    } else {
                        binding.cardSearchResults.visibility = View.GONE
                        binding.recyclerViewSearchResults.visibility = View.GONE
                        Toast.makeText(this@AddLocationActivity, "No results found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.cardSearchResults.visibility = View.GONE
                    binding.recyclerViewSearchResults.visibility = View.GONE
                    android.util.Log.e("AddLocation", "Search failed - Code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(this@AddLocationActivity, "Search failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.cardSearchResults.visibility = View.GONE
                binding.recyclerViewSearchResults.visibility = View.GONE
                android.util.Log.e("AddLocation", "Exception during search", e)
                Toast.makeText(this@AddLocationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarSearch.visibility = View.GONE
            }
        }
    }

    private fun setLocationMarker(latLng: LatLng) {
        if (!isMapReady) return
        
        selectedLocation = latLng
        
        // Remove existing marker
        currentMarker?.remove()
        
        // Add new marker
        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )
        
        // Move camera to selected location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        
        // Update coordinate display
        binding.tvSelectedCoordinates.text = "Selected: ${latLng.latitude}, ${latLng.longitude}"
        binding.tvSelectedCoordinates.visibility = View.VISIBLE
        
        // Enable save button
        binding.btnSaveLocation.isEnabled = true
    }

    private fun useMapCenter() {
        if (!isMapReady) return
        
        val center = googleMap.cameraPosition.target
        setLocationMarker(center)
    }

    private fun saveLocation() {
        val locationName = binding.etLocationName.text.toString().trim()
        val location = selectedLocation
        
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (location == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.progressBarSave.visibility = View.VISIBLE
        binding.btnSaveLocation.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val request = CreateLocationRequest(
                    name = locationName,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                
                val response = locationApi.addLocation(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddLocationActivity, "Location saved successfully", Toast.LENGTH_SHORT).show()
                    
                    // Return the location name and target for the calling activity
                    val resultIntent = Intent().apply {
                        putExtra("locationName", locationName)
                        putExtra("target", intent.getStringExtra("target"))
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@AddLocationActivity, "Failed to save location", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddLocationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarSave.visibility = View.GONE
                binding.btnSaveLocation.isEnabled = true
            }
        }
    }

    private fun testAPI() {
        Log.d("AddLocation", "Testing API directly...")
        lifecycleScope.launch {
            try {
                val response = locationApi.searchLocations("test")
                Log.d("AddLocation", "Direct API test - Code: ${response.code()}")
                Log.d("AddLocation", "Direct API test - Success: ${response.isSuccessful}")
                Log.d("AddLocation", "Direct API test - Body: ${response.body()}")
                Log.d("AddLocation", "Direct API test - Raw: ${response.raw()}")
                Log.d("AddLocation", "Direct API test - Error: ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.e("AddLocation", "Direct API test failed", e)
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}