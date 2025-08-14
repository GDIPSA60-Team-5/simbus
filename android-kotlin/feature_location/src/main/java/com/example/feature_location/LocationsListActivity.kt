package com.example.feature_location

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.core.api.LocationApi
import com.example.core.api.SavedLocation
import com.example.core.di.SecureStorageManager
import com.example.core.permission.LocationPermissionManager
import com.example.feature_location.databinding.ActivityLocationsListBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationsListActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationsListBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var locationPermissionManager: LocationPermissionManager
    private var savedLocations = mutableListOf<SavedLocation>()
    private var isMapReady = false

    @Inject
    lateinit var locationApi: LocationApi

    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupMap()
        setupClickListeners()
        loadUserLocations()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "My Locations"
        }
    }

    private fun setupRecyclerView() {
        locationsAdapter = LocationsAdapter { location ->
            // When a location is clicked, zoom to it on the map
            zoomToLocation(location)
        }
        
        binding.recyclerViewLocations.apply {
            layoutManager = LinearLayoutManager(this@LocationsListActivity)
            adapter = locationsAdapter
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
        
        // Display existing locations on map
        displayLocationsOnMap()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.fabAddLocation.setOnClickListener {
            val intent = Intent(this, com.example.feature_location.location.AddLocationActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_LOCATION)
        }
    }

    private fun loadUserLocations() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = locationApi.getUserLocations()
                if (response.isSuccessful && response.body() != null) {
                    savedLocations.clear()
                    savedLocations.addAll(response.body()!!)
                    
                    updateUI()
                    displayLocationsOnMap()
                } else {
                    Toast.makeText(this@LocationsListActivity, "Failed to load locations", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LocationsListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI() {
        if (savedLocations.isEmpty()) {
            binding.tvNoLocations.visibility = View.VISIBLE
            binding.recyclerViewLocations.visibility = View.GONE
        } else {
            binding.tvNoLocations.visibility = View.GONE
            binding.recyclerViewLocations.visibility = View.VISIBLE
            locationsAdapter.updateLocations(savedLocations)
        }
    }

    private fun displayLocationsOnMap() {
        if (!isMapReady) return
        
        googleMap.clear()
        
        savedLocations.forEach { location ->
            val position = LatLng(location.latitude, location.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(location.locationName)
            )
        }
        
        // If we have locations, fit them all in view
        if (savedLocations.isNotEmpty()) {
            if (savedLocations.size == 1) {
                val location = savedLocations[0]
                val position = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            } else {
                // Fit all markers in view
                val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                savedLocations.forEach { location ->
                    builder.include(LatLng(location.latitude, location.longitude))
                }
                val bounds = builder.build()
                val padding = 100 // pixels
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        }
    }

    private fun zoomToLocation(location: SavedLocation) {
        if (!isMapReady) return
        
        val position = LatLng(location.latitude, location.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        
        // Highlight the selected marker
        googleMap.clear()
        displayLocationsOnMap()
        
        // Add a special marker for the selected location
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(location.locationName)
        )?.showInfoWindow()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_LOCATION && resultCode == RESULT_OK) {
            loadUserLocations()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val REQUEST_ADD_LOCATION = 1001
    }
}