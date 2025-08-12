package com.example.feature_guidemap

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.core.model.Route
import com.example.core.model.RouteLeg
import com.example.feature_guidemap.databinding.ActivityMapsNavigationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsNavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsNavigationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private var routePolylines: MutableList<Polyline> = mutableListOf()
    
    private var selectedRoute: Route? = null
    private var currentLegIndex = 0
    private lateinit var legAdapter: LegInstructionAdapter

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize selectedRoute properly
        selectedRoute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("selected_route", Route::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("selected_route")
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
        supportActionBar?.hide()
        checkLocationPermissions()
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()

        selectedRoute?.let {
            setupViewPager()
            displayRoute()
        }
    }


    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupMap() {
        with(googleMap) {
            // Enable location if permission granted
            if (ContextCompat.checkSelfPermission(
                    this@MapsNavigationActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isMyLocationEnabled = true
                uiSettings.isMyLocationButtonEnabled = false
            }

            // Disable default UI elements since we have custom ones
            uiSettings.isCompassEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMapToolbarEnabled = false

            // Move camera to default location (Singapore)
            val singapore = LatLng(1.3521, 103.8198)
            moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 15f))
        }

        // Get current location and center map
        getCurrentLocation()
    }
    
    private fun displayRoute() {
        selectedRoute?.let {
            // Show all legs with different colors but initially focus on first leg
            focusOnLeg(0)
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Update current location marker
                    currentLocationMarker?.remove()
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    // Move camera to current location
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f)
                    )
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap()
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission required for navigation", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initViews() {
        // Set click listeners using binding
        binding.btnStartNavigation.setOnClickListener { startNavigation() }
        binding.fabZoomIn.setOnClickListener { zoomIn() }
        binding.fabZoomOut.setOnClickListener { zoomOut() }
    }
    
    private fun setupViewPager() {
        selectedRoute?.let { route ->
            legAdapter = LegInstructionAdapter(route.legs) { legIndex ->
                focusOnLeg(legIndex)
            }
            binding.vpLegInstructions.adapter = legAdapter
            
            // Set up page change listener
            binding.vpLegInstructions.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentLegIndex = position
                    focusOnLeg(position)
                    updatePageIndicators(position)
                }
            })
            
            // Initial setup
            setupPageIndicators(route.legs.size)
            focusOnLeg(0)
        }
    }
    
    private fun setupPageIndicators(count: Int) {
        binding.llPageIndicators.removeAllViews()
        
        for (i in 0 until count) {
            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    16, 16
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                background = ContextCompat.getDrawable(this@MapsNavigationActivity, R.drawable.direction_icon_background)
                alpha = if (i == 0) 1.0f else 0.3f
            }
            binding.llPageIndicators.addView(indicator)
        }
    }
    
    private fun updatePageIndicators(selectedIndex: Int) {
        for (i in 0 until binding.llPageIndicators.childCount) {
            binding.llPageIndicators.getChildAt(i).alpha = if (i == selectedIndex) 1.0f else 0.3f
        }
    }
    
    private fun focusOnLeg(legIndex: Int) {
        selectedRoute?.let { route ->
            if (legIndex < route.legs.size) {
                val leg = route.legs[legIndex]
                
                routePolylines.forEach { it.remove() }
                routePolylines.clear()
                googleMap.clear()
                
                // Draw polyline for current leg only
                leg.routePoints?.let { points ->
                    if (points.size >= 2) {
                        val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                        
                        val color = when (leg.type.uppercase()) {
                            "WALK" -> ContextCompat.getColor(this, R.color.direction_straight)
                            "BUS" -> ContextCompat.getColor(this, R.color.direction_right)
                            else -> ContextCompat.getColor(this, R.color.direction_destination)
                        }
                        
                        val polyline = googleMap.addPolyline(
                            PolylineOptions()
                                .addAll(latLngPoints)
                                .color(color)
                                .width(12f)
                        )
                        routePolylines.add(polyline)
                        
                        // Focus camera on this leg
                        val boundsBuilder = LatLngBounds.Builder()
                        latLngPoints.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        )
                        
                        // Add markers for start and end of leg
                        addLegMarkers(leg, latLngPoints.first(), latLngPoints.last())
                    }
                }
            }
        }
    }
    
    private fun addLegMarkers(leg: RouteLeg, startPos: LatLng, endPos: LatLng) {
        // Add start marker
        val startTitle = when (leg.type.uppercase()) {
            "WALK" -> "Start walking"
            "BUS" -> leg.fromStopName ?: "Bus stop"
            else -> "Start"
        }
        
        googleMap.addMarker(
            MarkerOptions()
                .position(startPos)
                .title(startTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        
        // Add end marker
        val endTitle = when (leg.type.uppercase()) {
            "WALK" -> leg.toStopName ?: "Destination"
            "BUS" -> leg.toStopName ?: "Bus stop"
            else -> "End"
        }
        
        googleMap.addMarker(
            MarkerOptions()
                .position(endPos)
                .title(endTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun startNavigation() {
        binding.btnStartNavigation.visibility = View.GONE
        
    }


    private fun zoomIn() {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn())
        
        // Add bounce animation to the button
        binding.fabZoomIn.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                binding.fabZoomIn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun zoomOut() {
        googleMap.animateCamera(CameraUpdateFactory.zoomOut())
        
        // Add bounce animation to the button
        binding.fabZoomOut.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                binding.fabZoomOut.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun endJourney() {
        finish()
    }
}