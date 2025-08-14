package com.example.feature_guidemap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.app.Dialog
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.core.permission.LocationPermissionManager
import com.example.core.model.Route
import com.example.core.model.RouteLeg
import com.example.core.model.Trip
import com.example.core.service.TripService
import com.example.core.api.UserApi
import com.example.core.api.BusApi
import com.example.core.model.BusArrival
import com.example.feature_notification.BusArrivalNotificationService
import com.example.feature_guidemap.databinding.ActivityMapsNavigationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

@AndroidEntryPoint
class MapsNavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsNavigationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private var routePolylines: MutableList<Polyline> = mutableListOf()
    private var isMapReady = false
    private var hasLocationPermission = false
    
    private var selectedRoute: Route? = null
    private var currentLegIndex = 0
    private lateinit var legAdapter: LegInstructionAdapter
    private var currentTrip: Trip? = null
    
    // Navigation modes
    private var isActiveMode = false
    private var previewRoute: Route? = null
    
    // Location tracking for active navigation
    private var locationCallback: LocationCallback? = null
    private var isLocationUpdatesActive = false
    
    // Permission management
    private lateinit var locationPermissionManager: LocationPermissionManager
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionManager.handlePermissionResult(permissions)
    }
    
    @Inject
    lateinit var tripService: TripService
    
    @Inject
    lateinit var userApi: UserApi
    
    @Inject
    lateinit var busApi: BusApi

    @Inject
    lateinit var busArrivalNotificationService: BusArrivalNotificationService

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize previewRoute from intent
        previewRoute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("selected_route", Route::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("selected_route")
        }

        locationPermissionManager =
            LocationPermissionManager(activity = this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
        supportActionBar?.hide()
        
        // Check if we already have location permissions
        hasLocationPermission = locationPermissionManager.hasLocationPermissions()
        
        // Request permissions if not granted
        if (!hasLocationPermission) {
            requestLocationPermissions()
        }
        
        initializeNavigationMode()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        
        // Setup map if we already have location permission
        if (hasLocationPermission) {
            setupMapWithLocation()
        } else {
            setupMapBasic()
        }
        
        setupRouteDisplay()
    }


    private fun setupMapBasic() {
        with(googleMap) {
            // Disable default UI elements since we have custom ones
            uiSettings.isCompassEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMapToolbarEnabled = false

            // Move camera to default location (Singapore)
            val singapore = LatLng(1.3521, 103.8198)
            moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 15f))
        }
    }
    
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupMapWithLocation() {
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
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
        selectedRoute?.let { route ->
            if (isActiveMode) {
                // Active mode: focus on current leg
                focusOnLeg(currentLegIndex)
            } else {
                // Inactive mode: show entire route
                displayFullRoute(route)
            }
        }
    }
    
    private fun displayFullRoute(route: Route) {
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
        // Clear existing polylines and markers
        routePolylines.forEach { it.remove() }
        routePolylines.clear()
        googleMap.clear()
        
        var allLatLngPoints = mutableListOf<LatLng>()
        
        // Draw all route legs with consistent styling
        route.legs.forEachIndexed { index, leg ->
            leg.routePoints?.let { points ->
                if (points.size >= 2) {
                    val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                    allLatLngPoints.addAll(latLngPoints)
                    
                    // Use different colors for different transport modes
                    val color = when (leg.type.uppercase()) {
                        "WALK" -> ContextCompat.getColor(this, R.color.direction_straight)
                        "BUS" -> ContextCompat.getColor(this, R.color.direction_right)
                        else -> ContextCompat.getColor(this, R.color.direction_destination)
                    }
                    
                    val polyline = googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(latLngPoints)
                            .color(color)
                            .width(8f)
                    )
                    routePolylines.add(polyline)
                }
            }
        }
        
        // Add start and end markers
        if (route.legs.isNotEmpty()) {
            val firstLeg = route.legs.first()
            val lastLeg = route.legs.last()
            
            firstLeg.routePoints?.firstOrNull()?.let { startPoint ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(startPoint.latitude, startPoint.longitude))
                        .title("Start")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }
            
            lastLeg.routePoints?.lastOrNull()?.let { endPoint ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(endPoint.latitude, endPoint.longitude))
                        .title("Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            }
        }
        
        // Fit camera to show entire route with padding for instruction card
        if (allLatLngPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            allLatLngPoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            
            // Adjust bounds to account for instruction card at bottom
            val instructionCardHeightDp = 240f
            val instructionCardHeightPx = instructionCardHeightDp * resources.displayMetrics.density
            
            // Get screen dimensions
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels
            
            // Calculate effective map area (excluding instruction card)
            val effectiveMapHeight = screenHeight - instructionCardHeightPx
            
            // Expand bounds slightly to account for the bottom instruction card
            val southwest = bounds.southwest
            val northeast = bounds.northeast
            val latSpan = northeast.latitude - southwest.latitude
            
            // Add extra space at the bottom by shifting the bounds up
            val bottomPadding = latSpan * 0.3 // Add 30% more space at bottom
            val adjustedBounds = LatLngBounds(
                LatLng(southwest.latitude - bottomPadding, southwest.longitude),
                northeast
            )
            
            try {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(adjustedBounds, 100)
                )
            } catch (e: Exception) {
                // Fallback to simple zoom if bounds are too small
                val center = bounds.center
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(center, 15f)
                )
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionManager.requestLocationPermissions(permissionLauncher) { granted ->
            hasLocationPermission = granted
            if (granted) {
                // Setup map with location if map is ready
                if (isMapReady && ::googleMap.isInitialized) {
                    setupMapWithLocation()
                }
            } else {
                Toast.makeText(this, "Location permission is required for navigation", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation() {
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
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


    private fun initViews() {
        // Set click listeners using binding
        binding.btnStartNavigation.setOnClickListener { startNavigation() }
        binding.fabZoomIn.setOnClickListener { zoomIn() }
        binding.fabZoomOut.setOnClickListener { zoomOut() }
        binding.fabBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSimulateNext.setOnClickListener { simulateNextLeg() }
        binding.btnEndTrip.setOnClickListener { showEndTripConfirmation() }
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
                    if (isActiveMode) {
                        updateTripProgress(position)
                    } else {
                        currentLegIndex = position
                    }
                    focusOnLeg(position)
                    updatePageIndicators(position)
                    
                    // Show bus arrivals for active mode when viewing walking legs
                    if (isActiveMode) {
                        showBusArrivalsForActiveLeg(position)
                    }
                }
            })
            
            // Initial setup
            setupPageIndicators(route.legs.size)
            
            // Set ViewPager to current leg (important for when returning to activity)
            binding.vpLegInstructions.setCurrentItem(currentLegIndex, false)
            
            focusOnLeg(currentLegIndex)
            updatePageIndicators(currentLegIndex)
            
            // Show bus arrivals for the initial leg in active mode
            if (isActiveMode) {
                showBusArrivalsForActiveLeg(currentLegIndex)
            }
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
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
        selectedRoute?.let { route ->
            if (legIndex < route.legs.size) {
                // Clear existing polylines and markers
                routePolylines.forEach { it.remove() }
                routePolylines.clear()
                googleMap.clear()
                
                // Draw all route legs with different styles
                var allLatLngPoints = mutableListOf<LatLng>()
                
                route.legs.forEachIndexed { index, leg ->
                    leg.routePoints?.let { points ->
                        if (points.size >= 2) {
                            val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                            allLatLngPoints.addAll(latLngPoints)
                            
                            val polyline = when {
                                index < legIndex -> {
                                    // Previous legs - grey and thin
                                    googleMap.addPolyline(
                                        PolylineOptions()
                                            .addAll(latLngPoints)
                                            .color(android.graphics.Color.GRAY)
                                            .width(6f)
                                            .pattern(listOf(Dash(10f), Gap(5f)))
                                    )
                                }
                                index == legIndex -> {
                                    // Current leg - highlighted
                                    val color = when (leg.type.uppercase()) {
                                        "WALK" -> ContextCompat.getColor(this, R.color.direction_straight)
                                        "BUS" -> ContextCompat.getColor(this, R.color.direction_right)
                                        else -> ContextCompat.getColor(this, R.color.direction_destination)
                                    }
                                    googleMap.addPolyline(
                                        PolylineOptions()
                                            .addAll(latLngPoints)
                                            .color(color)
                                            .width(12f)
                                    )
                                }
                                else -> {
                                    // Future legs - light grey and thin
                                    googleMap.addPolyline(
                                        PolylineOptions()
                                            .addAll(latLngPoints)
                                            .color(android.graphics.Color.LTGRAY)
                                            .width(4f)
                                    )
                                }
                            }
                            routePolylines.add(polyline)
                        }
                    }
                }
                
                // Add markers for current leg
                val currentLeg = route.legs[legIndex]
                currentLeg.routePoints?.let { points ->
                    if (points.size >= 2) {
                        val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                        addLegMarkers(currentLeg, latLngPoints.first(), latLngPoints.last())
                    }
                }
                
                // Focus camera on current leg with padding for instruction card
                currentLeg.routePoints?.let { points ->
                    if (points.size >= 2) {
                        val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                        val boundsBuilder = LatLngBounds.Builder()
                        latLngPoints.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        
                        // Adjust bounds to account for instruction card at bottom
                        val instructionCardHeightDp = 240f
                        val instructionCardHeightPx = instructionCardHeightDp * resources.displayMetrics.density
                        
                        // Get screen dimensions
                        val displayMetrics = resources.displayMetrics
                        val screenHeight = displayMetrics.heightPixels
                        val screenWidth = displayMetrics.widthPixels
                        
                        // Calculate effective map area (excluding instruction card)
                        val effectiveMapHeight = screenHeight - instructionCardHeightPx
                        val mapAspectRatio = screenWidth / effectiveMapHeight
                        
                        // Expand bounds slightly to account for the bottom instruction card
                        val southwest = bounds.southwest
                        val northeast = bounds.northeast
                        val latSpan = northeast.latitude - southwest.latitude
                        val lngSpan = northeast.longitude - southwest.longitude
                        
                        // Add extra space at the bottom by shifting the bounds up
                        val bottomPadding = latSpan * 0.3 // Add 30% more space at bottom
                        val adjustedBounds = LatLngBounds(
                            LatLng(southwest.latitude - bottomPadding, southwest.longitude),
                            northeast
                        )
                        
                        try {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(adjustedBounds, 100)
                            )
                        } catch (e: Exception) {
                            // Fallback to simple zoom if bounds are too small
                            val center = bounds.center
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(center, 15f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun addLegMarkers(leg: RouteLeg, startPos: LatLng, endPos: LatLng) {
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
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
        if (!isActiveMode && selectedRoute != null) {
            // Start the trip by saving it to the database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get current user first
                    val userResponse = userApi.getCurrentUser()
                    if (!userResponse.isSuccessful || userResponse.body() == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MapsNavigationActivity, "Failed to get user info", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                    
                    val username = userResponse.body()!!.username
                    val startLocation = intent.getStringExtra("start_location") ?: "Unknown Start"
                    val endLocation = intent.getStringExtra("end_location") ?: "Unknown End"
                    
                    // Get coordinates from intent if available
                    val startCoordinates = if (intent.hasExtra("start_latitude") && intent.hasExtra("start_longitude")) {
                        com.example.core.model.Coordinates(
                            intent.getDoubleExtra("start_latitude", 0.0),
                            intent.getDoubleExtra("start_longitude", 0.0)
                        )
                    } else null
                    
                    val endCoordinates = if (intent.hasExtra("end_latitude") && intent.hasExtra("end_longitude")) {
                        com.example.core.model.Coordinates(
                            intent.getDoubleExtra("end_latitude", 0.0),
                            intent.getDoubleExtra("end_longitude", 0.0)
                        )
                    } else null
                    
                    val result = tripService.startTrip(
                        username = username,
                        startLocation = startLocation,
                        endLocation = endLocation,
                        startCoordinates = startCoordinates,
                        endCoordinates = endCoordinates,
                        route = selectedRoute!!
                    )
                    
                    withContext(Dispatchers.Main) {
                        result.fold(
                            onSuccess = { trip ->
                                // Switch to active mode
                                enterActiveMode(trip)
                                Toast.makeText(this@MapsNavigationActivity, "Trip started!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { error ->
                                Toast.makeText(this@MapsNavigationActivity, "Failed to start trip: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Error starting trip: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun zoomIn() {
        if (!isMapReady || !::googleMap.isInitialized) return
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
        if (!isMapReady || !::googleMap.isInitialized) return
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

    private fun initializeNavigationMode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check for active trip first
                val userResponse = userApi.getCurrentUser()
                if (!userResponse.isSuccessful || userResponse.body() == null) {
                    withContext(Dispatchers.Main) {
                        handlePreviewMode()
                    }
                    return@launch
                }
                
                val username = userResponse.body()!!.username
                val result = tripService.getActiveTrip(username)
                result.fold(
                    onSuccess = { activeTrip ->
                        withContext(Dispatchers.Main) {
                            if (activeTrip != null) {
                                // Active trip exists
                                if (previewRoute != null) {
                                    // Show warning modal about ending current journey
                                    showEndJourneyWarning(activeTrip)
                                } else {
                                    // Enter active mode
                                    enterActiveMode(activeTrip)
                                }
                            } else {
                                // No active trip, use preview mode if route provided
                                handlePreviewMode()
                            }
                        }
                    },
                    onFailure = { 
                        withContext(Dispatchers.Main) {
                            handlePreviewMode()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handlePreviewMode()
                }
            }
        }
    }
    
    private fun handlePreviewMode() {
        previewRoute?.let { route ->
            enterPreviewMode(route)
        } ?: run {
            // No route provided and no active trip, hide instruction card
            hideInstructionCard()
        }
    }
    
    private fun enterActiveMode(trip: Trip) {
        isActiveMode = true
        currentTrip = trip
        selectedRoute = trip.route
        currentLegIndex = trip.currentLegIndex
        
        // Hide start button, show active mode buttons and instruction card
        binding.btnStartNavigation.visibility = View.GONE
        binding.llActiveButtons.visibility = View.VISIBLE
        showInstructionCard()
        
        setupRouteDisplay()
        startLocationTracking()
        
        // Start bus arrival monitoring
        busArrivalNotificationService.startBusArrivalMonitoring()
        
        // Check bus arrivals immediately for current leg
        CoroutineScope(Dispatchers.IO).launch {
            busArrivalNotificationService.checkBusArrivalsForCurrentTrip()
        }
    }
    
    private fun enterPreviewMode(route: Route) {
        isActiveMode = false
        selectedRoute = route
        currentLegIndex = 0
        
        // Show start button, hide active mode buttons and show instruction card (inactive mode)
        binding.btnStartNavigation.visibility = View.VISIBLE
        binding.llActiveButtons.visibility = View.GONE
        showInstructionCard()
        
        setupRouteDisplay()
        stopLocationTracking()
    }
    
    private fun setupRouteDisplay() {
        selectedRoute?.let { route ->
            if (isActiveMode) {
                setupActiveMode()
            } else {
                setupInactiveMode()
            }
            displayRoute()
        }
    }
    
    private fun setupActiveMode() {
        // Show active mode layout with ViewPager
        binding.root.findViewById<View>(R.id.layout_active_mode).visibility = View.VISIBLE
        binding.root.findViewById<View>(R.id.layout_inactive_mode).visibility = View.GONE
        
        selectedRoute?.let { route ->
            setupViewPager()
        }
    }
    
    private fun setupInactiveMode() {
        // Show inactive mode layout with all directions
        binding.root.findViewById<View>(R.id.layout_active_mode).visibility = View.GONE
        binding.root.findViewById<View>(R.id.layout_inactive_mode).visibility = View.VISIBLE
        
        selectedRoute?.let { route ->
            setupAllDirectionsView(route)
        }
    }
    
    private fun setupAllDirectionsView(route: Route) {
        val directionsContainer = binding.root.findViewById<LinearLayout>(R.id.ll_all_directions)
        directionsContainer.removeAllViews()
        
        route.legs.forEachIndexed { index, leg ->
            val stepView = layoutInflater.inflate(R.layout.item_direction_step, directionsContainer, false)
            
            val stepNumber = stepView.findViewById<TextView>(R.id.tv_step_number)
            val direction = stepView.findViewById<TextView>(R.id.tv_direction)
            val details = stepView.findViewById<TextView>(R.id.tv_details)
            val serviceInfo = stepView.findViewById<TextView>(R.id.tv_service_info)
            val transportIcon = stepView.findViewById<ImageView>(R.id.iv_transport_icon)
            
            stepNumber.text = (index + 1).toString()
            
            when (leg.type.uppercase()) {
                "WALK" -> {
                    direction.text = if (index == 0) "Walk to ${leg.toStopName ?: "destination"}" 
                                   else "Walk to ${leg.toStopName ?: "next location"}"
                    details.text = "${leg.durationInMinutes} min walk"
                    transportIcon.setImageResource(R.drawable.ic_walk)
                    transportIcon.setColorFilter(ContextCompat.getColor(this, R.color.direction_straight))
                    serviceInfo.visibility = View.GONE
                }
                "BUS" -> {
                    direction.text = "Take Bus ${leg.busServiceNumber ?: ""}"
                    details.text = "${leg.durationInMinutes} min ride"
                    serviceInfo.text = "From: ${leg.fromStopName ?: "Bus Stop"}\nTo: ${leg.toStopName ?: "Bus Stop"}"
                    serviceInfo.visibility = View.VISIBLE
                    transportIcon.setImageResource(R.drawable.ic_bus)
                    transportIcon.setColorFilter(ContextCompat.getColor(this, R.color.direction_right))
                }
                else -> {
                    direction.text = leg.type.replaceFirstChar { it.titlecase() }
                    details.text = "${leg.durationInMinutes} min"
                    transportIcon.setImageResource(R.drawable.ic_walk)
                    serviceInfo.visibility = View.GONE
                }
            }
            
            directionsContainer.addView(stepView)
            
            // Add bus arrivals after walking legs that lead to bus stops
            if (leg.type.uppercase() == "WALK" && index + 1 < route.legs.size) {
                val nextLeg = route.legs[index + 1]
                if (nextLeg.type.uppercase() == "BUS" && leg.toStopName != null) {
                    // Get the correct bus service number
                    val serviceNumber = getBusServiceNumberForStop(route, index)
                    addBusArrivalsView(directionsContainer, leg.toStopName!!, serviceNumber)
                }
            }
        }
    }
    
    private fun getBusServiceNumberForStop(route: Route, walkLegIndex: Int): String? {
        // If the current leg is WALK, get the service number from the next BUS leg
        val nextLegIndex = walkLegIndex + 1
        
        // Make sure there's a next leg and it's a BUS
        if (nextLegIndex < route.legs.size) {
            val nextLeg = route.legs[nextLegIndex]
            if (nextLeg.type.uppercase() == "BUS") {
                return nextLeg.busServiceNumber
            }
        }
        
        // If we can't find a bus leg, check if we need to look at the second leg
        // (in case the first leg is WALK and we're at index 0)
        if (walkLegIndex == 0 && route.legs.size > 1) {
            val secondLeg = route.legs[1]
            if (secondLeg.type.uppercase() == "BUS") {
                return secondLeg.busServiceNumber
            }
        }
        
        // Fallback: no service number available
        return null
    }
    
    private fun addBusArrivalsView(container: LinearLayout, busStopName: String, busServiceNumber: String?) {
        val busArrivalsView = layoutInflater.inflate(R.layout.item_bus_arrivals, container, false)
        val busServicesContainer = busArrivalsView.findViewById<LinearLayout>(R.id.ll_bus_services)
        val loadingIndicator = busArrivalsView.findViewById<ProgressBar>(R.id.pb_loading)
        
        container.addView(busArrivalsView)
        
        // Show loading indicator
        loadingIndicator.visibility = View.VISIBLE
        
        // Fetch bus arrivals
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = busApi.getBusArrivals(busStopName, busServiceNumber)
                withContext(Dispatchers.Main) {
                    loadingIndicator.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        displayBusArrivals(busServicesContainer, response.body()!!)
                    } else {
                        showBusArrivalsError(busServicesContainer)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingIndicator.visibility = View.GONE
                    showBusArrivalsError(busServicesContainer)
                }
            }
        }
    }
    
    private fun displayBusArrivals(container: LinearLayout, busArrivals: List<BusArrival>) {
        container.removeAllViews()
        
        if (busArrivals.isEmpty()) {
            val noDataText = TextView(this).apply {
                text = "No bus arrival data available"
                setTextColor(ContextCompat.getColor(this@MapsNavigationActivity, android.R.color.darker_gray))
                textSize = 12f
            }
            container.addView(noDataText)
            return
        }
        
        busArrivals.forEach { busArrival ->
            val serviceView = layoutInflater.inflate(R.layout.item_bus_service, container, false)
            
            val serviceName = serviceView.findViewById<TextView>(R.id.tv_service_name)
            val nextArrival = serviceView.findViewById<TextView>(R.id.tv_next_arrival)
            val followingArrivals = serviceView.findViewById<TextView>(R.id.tv_following_arrivals)
            
            serviceName.text = busArrival.serviceName
            
            if (busArrival.arrivals.isNotEmpty()) {
                try {
                    val now = OffsetDateTime.now()
                    
                    // Parse first arrival time - handle the timezone offset format
                    val firstArrival = OffsetDateTime.parse(busArrival.arrivals[0])
                    val minutesUntilArrival = ChronoUnit.MINUTES.between(now, firstArrival)
                    
                    nextArrival.text = when {
                        minutesUntilArrival <= 0 -> "Now"
                        minutesUntilArrival == 1L -> "1 min"
                        else -> "${minutesUntilArrival} min"
                    }
                    
                    // Show following arrivals (next 2-3)
                    if (busArrival.arrivals.size > 1) {
                        val followingTimes = busArrival.arrivals.drop(1).take(2).map { arrivalTime ->
                            val arrival = OffsetDateTime.parse(arrivalTime)
                            val minutes = ChronoUnit.MINUTES.between(now, arrival)
                            "${minutes} min"
                        }
                        followingArrivals.text = "• ${followingTimes.joinToString(" • ")}"
                    } else {
                        followingArrivals.visibility = View.GONE
                    }
                    
                } catch (e: Exception) {
                    nextArrival.text = "-- min"
                    followingArrivals.visibility = View.GONE
                }
            } else {
                nextArrival.text = "No data"
                followingArrivals.visibility = View.GONE
            }
            
            container.addView(serviceView)
        }
    }
    
    private fun showBusArrivalsError(container: LinearLayout) {
        container.removeAllViews()
        val errorText = TextView(this).apply {
            text = "Unable to load bus arrivals"
            setTextColor(ContextCompat.getColor(this@MapsNavigationActivity, android.R.color.darker_gray))
            textSize = 12f
        }
        container.addView(errorText)
    }
    
    private fun showBusArrivalsForActiveLeg(legIndex: Int) {
        selectedRoute?.let { route ->
            val activeBusArrivalsContainer = binding.root.findViewById<LinearLayout>(R.id.ll_active_bus_arrivals)
            activeBusArrivalsContainer.removeAllViews()
            activeBusArrivalsContainer.visibility = View.GONE
            
            // Check if current leg is WALK and leads to a bus stop
            if (legIndex < route.legs.size) {
                val currentLeg = route.legs[legIndex]
                
                if (currentLeg.type.uppercase() == "WALK" && legIndex + 1 < route.legs.size) {
                    val nextLeg = route.legs[legIndex + 1]
                    if (nextLeg.type.uppercase() == "BUS" && currentLeg.toStopName != null) {
                        // Get the correct bus service number
                        val serviceNumber = getBusServiceNumberForStop(route, legIndex)
                        
                        activeBusArrivalsContainer.visibility = View.VISIBLE
                        addBusArrivalsViewToContainer(activeBusArrivalsContainer, currentLeg.toStopName!!, serviceNumber)
                    }
                }
            }
        }
    }
    
    private fun addBusArrivalsViewToContainer(parentContainer: LinearLayout, busStopName: String, busServiceNumber: String?) {
        val busArrivalsView = layoutInflater.inflate(R.layout.item_bus_arrivals, parentContainer, false)
        val busServicesContainer = busArrivalsView.findViewById<LinearLayout>(R.id.ll_bus_services)
        val loadingIndicator = busArrivalsView.findViewById<ProgressBar>(R.id.pb_loading)
        
        parentContainer.addView(busArrivalsView)
        
        // Show loading indicator
        loadingIndicator.visibility = View.VISIBLE
        
        // Fetch bus arrivals
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = busApi.getBusArrivals(busStopName, busServiceNumber)
                withContext(Dispatchers.Main) {
                    loadingIndicator.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        displayBusArrivals(busServicesContainer, response.body()!!)
                    } else {
                        showBusArrivalsError(busServicesContainer)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingIndicator.visibility = View.GONE
                    showBusArrivalsError(busServicesContainer)
                }
            }
        }
    }
    
    private fun showInstructionCard() {
        binding.bottomControls.visibility = View.VISIBLE
    }
    
    private fun hideInstructionCard() {
        binding.bottomControls.visibility = View.GONE
        binding.llActiveButtons.visibility = View.GONE
    }
    
    private fun showEndJourneyWarning(activeTrip: Trip) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Active Journey")
            .setMessage("You have an active journey to ${activeTrip.endLocation}. Starting a new route will end your current journey. Do you want to continue?")
            .setPositiveButton("End Current Journey") { _, _ ->
                endCurrentTripAndStartNew(activeTrip)
            }
            .setNegativeButton("Keep Current Journey") { _, _ ->
                enterActiveMode(activeTrip)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun endCurrentTripAndStartNew(currentTrip: Trip) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                tripService.completeTrip(currentTrip.id)
                withContext(Dispatchers.Main) {
                    busArrivalNotificationService.stopBusArrivalMonitoring()
                    previewRoute?.let { route ->
                        enterPreviewMode(route)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapsNavigationActivity, "Error ending current trip: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    
    private fun updateTripProgress(newLegIndex: Int) {
        if (isActiveMode) {
            currentTrip?.let { trip ->
                if (newLegIndex != currentLegIndex) {
                    currentLegIndex = newLegIndex
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = tripService.updateTripProgress(trip.id, newLegIndex)
                            result.onSuccess { updatedTrip ->
                                withContext(Dispatchers.Main) {
                                    // Update the local trip object to reflect the new state
                                    currentTrip = updatedTrip
                                }
                                
                                // Check bus arrivals for the new leg
                                busArrivalNotificationService.checkBusArrivalsForCurrentTrip()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MapsNavigation", "Error updating trip progress", e)
                        }
                    }
                }
            }
        }
    }
    
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationTracking() {
        if (isLocationUpdatesActive) return
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateCurrentLocationMarker(location)
                    if (isActiveMode) {
                        checkLegProgression(location)
                    }
                }
            }
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Update every 5 seconds
        ).build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
            isLocationUpdatesActive = true
        } catch (e: SecurityException) {
            android.util.Log.e("MapsNavigation", "Location permission not granted", e)
        }
    }
    
    private fun stopLocationTracking() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
            isLocationUpdatesActive = false
        }
    }
    
    private fun updateCurrentLocationMarker(location: Location) {
        if (!isMapReady || !::googleMap.isInitialized) {
            return
        }
        
        val currentLatLng = LatLng(location.latitude, location.longitude)
        
        // Update current location marker
        currentLocationMarker?.remove()
        currentLocationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }
    
    private fun checkLegProgression(currentLocation: Location) {
        selectedRoute?.let { route ->
            if (currentLegIndex < route.legs.size) {
                val currentLeg = route.legs[currentLegIndex]
                val endPoint = currentLeg.routePoints?.lastOrNull()
                
                endPoint?.let { legEndPoint ->
                    val legEndLocation = Location("").apply {
                        latitude = legEndPoint.latitude
                        longitude = legEndPoint.longitude
                    }
                    
                    val distanceToEnd = currentLocation.distanceTo(legEndLocation)
                    
                    // If within 50 meters of leg end, progress to next leg
                    if (distanceToEnd <= 50f) {
                        val nextLegIndex = currentLegIndex + 1
                        if (nextLegIndex < route.legs.size) {
                            // Progress to next leg
                            updateTripProgress(nextLegIndex)
                            binding.vpLegInstructions.setCurrentItem(nextLegIndex, true)
                            focusOnLeg(nextLegIndex)
                            
                            Toast.makeText(this, "Progressing to next step", Toast.LENGTH_SHORT).show()
                        } else {
                            // Journey completed
                            completeJourney()
                        }
                    }
                }
            }
        }
    }
    
    private fun completeJourney() {
        currentTrip?.let { trip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    tripService.completeTrip(trip.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Journey completed!", Toast.LENGTH_LONG).show()
                        stopLocationTracking()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Error completing journey: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun endJourney() {
        currentTrip?.let { trip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    tripService.completeTrip(trip.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Trip completed!", Toast.LENGTH_SHORT).show()
                        stopLocationTracking()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Error completing trip: ${e.message}", Toast.LENGTH_LONG).show()
                        stopLocationTracking()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        finish()
                    }
                }
            }
        } ?: run {
            stopLocationTracking()
            busArrivalNotificationService.stopBusArrivalMonitoring()
            finish()
        }
    }
    
    private fun simulateNextLeg() {
        selectedRoute?.let { route ->
            if (isActiveMode && currentTrip != null) {
                val nextLegIndex = currentLegIndex + 1
                
                if (nextLegIndex < route.legs.size) {
                    // Move to next leg
                    currentLegIndex = nextLegIndex
                    updateTripProgress(nextLegIndex)
                    binding.vpLegInstructions.setCurrentItem(nextLegIndex, true)
                    focusOnLeg(nextLegIndex)
                    showBusArrivalsForActiveLeg(nextLegIndex)
                    
                    Toast.makeText(this, "Simulated progress to leg ${nextLegIndex + 1}", Toast.LENGTH_SHORT).show()
                } else {
                    // Journey completed
                    simulateJourneyCompletion()
                }
            }
        }
    }
    
    private fun simulateJourneyCompletion() {
        currentTrip?.let { trip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    tripService.completeTrip(trip.id)
                    withContext(Dispatchers.Main) {
                        stopLocationTracking()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        showTripCompletionDialog()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Error completing trip: ${e.message}", Toast.LENGTH_LONG).show()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        showTripCompletionDialog() // Still show dialog even if DB update fails
                    }
                }
            }
        }
    }
    
    private fun showTripCompletionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_trip_completed)
        dialog.setCancelable(false)
        
        val backToHomeButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_back_to_home)
        backToHomeButton.setOnClickListener {
            dialog.dismiss()
            navigateToHome()
        }
        
        dialog.show()
    }
    
    private fun navigateToHome() {
        // Navigate back to the main activity (home)
        val intent = Intent().apply {
            // Set flags to clear the task and start fresh
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        // Finish this activity and let the back navigation handle returning to home
        finish()
    }
    
    private fun showEndTripConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("End Trip")
            .setMessage("Are you sure you want to end your current trip? This action cannot be undone.")
            .setPositiveButton("End Trip") { _, _ ->
                endCurrentTrip()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun endCurrentTrip() {
        currentTrip?.let { trip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    tripService.completeTrip(trip.id)
                    withContext(Dispatchers.Main) {
                        stopLocationTracking()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        binding.llActiveButtons.visibility = View.GONE
                        Toast.makeText(this@MapsNavigationActivity, "Trip ended", Toast.LENGTH_SHORT).show()
                        
                        // Show completion dialog or just finish
                        showTripEndedDialog()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapsNavigationActivity, "Error ending trip: ${e.message}", Toast.LENGTH_LONG).show()
                        busArrivalNotificationService.stopBusArrivalMonitoring()
                        // Still show dialog even if DB update fails
                        showTripEndedDialog()
                    }
                }
            }
        }
    }
    
    private fun showTripEndedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Trip Ended")
            .setMessage("Your trip has been ended. You can start a new navigation anytime.")
            .setPositiveButton("Back to Home") { _, _ ->
                navigateToHome()
            }
            .setCancelable(false)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        busArrivalNotificationService.stopBusArrivalMonitoring()
        // Hide active mode buttons when destroying
        binding.llActiveButtons.visibility = View.GONE
    }
}