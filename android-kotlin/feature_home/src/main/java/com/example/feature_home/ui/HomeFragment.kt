package com.example.feature_home.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.core.di.SecureStorageManager
import com.example.core.service.TripService
import com.example.core.model.Trip
import com.example.feature_home.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import android.content.Intent
import com.example.feature_guidemap.MapsNavigationActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.core.api.UserApi
import com.example.core.api.CommuteApi
import com.example.core.model.RouteLeg
import com.example.feature_chatbot.ui.ChatbotActivity
import com.example.feature_home.adapter.DailyCommuteAdapter
import com.example.feature_home.adapter.DayCommutes
import com.example.feature_home.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_UPDATE_INTERVAL = 10_000L
        private const val MARKER_TITLE = "You are here"
    }

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var commuteApi: CommuteApi

    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    @Inject
    lateinit var tripService: TripService

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMapInstance: GoogleMap? = null
    private lateinit var dailyCommuteAdapter: DailyCommuteAdapter
    private var currentTrip: Trip? = null
    private var routePolylines: MutableList<Polyline> = mutableListOf()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            enableLocationAndZoom()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateMapWithLocation(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(savedInstanceState)
        setupOnClickListeners()
        loadUserData()
        loadCommuteData()
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        setupWindowInsets()
        setupLocationClient()
        setupMapView(savedInstanceState)
        setupCommuteViewPager()
        setupBackPressHandler()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setupOnClickListeners() {
        binding.micIcon.setOnClickListener {
            // Your existing mic logic here (if any)
        }
        binding.sendIcon.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val intent = Intent(requireContext(), ChatbotActivity::class.java)
                intent.putExtra("userMessage", text)
                startActivity(intent)
            }
        }
        
        // Navigation FAB click listener
        binding.fabNavigation.setOnClickListener {
            val intent = Intent(requireContext(), MapsNavigationActivity::class.java)
            startActivity(intent)
        }

        binding.button3.setOnClickListener {
            val intent = Intent(requireContext(), TripHistoryActivity::class.java)
            startActivity(intent)
        }
        // Active trip overlay click listener
        binding.layoutActiveTrip.setOnClickListener {
            val intent = Intent(requireContext(), MapsNavigationActivity::class.java)
            startActivity(intent)
        }
        
        // Collapse button for active trip overlay
        binding.ivCollapse.setOnClickListener {
            collapseActiveTripOverlay()
        }
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@HomeFragment)
        }
    }

    private fun setupCommuteViewPager() {
        dailyCommuteAdapter = DailyCommuteAdapter()
        binding.apply {
            viewPager.adapter = dailyCommuteAdapter
            viewPager.setOffscreenPageLimit(2);
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun hasLocationPermission(): Boolean =
        LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMapInstance = googleMap
        handleLocationPermissionAndSetup()
        checkForActiveTrip()

        googleMap.setOnMapClickListener {
            val context = requireContext()
            val intent = Intent(context, MapsNavigationActivity::class.java)
            startActivity(intent)
        }
    }


    private fun handleLocationPermissionAndSetup() {
        if (hasLocationPermission()) {
            enableLocationAndZoom()
        } else {
            locationPermissionRequest.launch(LOCATION_PERMISSIONS)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationAndZoom() {
        if (!hasLocationPermission()) return

        googleMapInstance?.let { map ->
            map.isMyLocationEnabled = true
            getLocationAndUpdateMap()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndUpdateMap() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                handleLocationResult(location)
            }
            .addOnFailureListener {
                requestCurrentLocation()
            }
    }

    private fun handleLocationResult(location: Location?) {
        if (location != null) {
            updateMapWithLocation(location)
        } else {
            requestCurrentLocation()
        }
    }

    private fun updateMapWithLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        googleMapInstance?.apply {
            animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM))
            clear()
            addMarker(MarkerOptions().position(userLatLng).title(MARKER_TITLE))
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        if (!hasLocationPermission()) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            requireActivity().mainLooper
        )
    }

    // MapView lifecycle delegation - grouped for clarity
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }


    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun loadUserData() {
        val token = secureStorageManager.getToken()
        if (token != null) {
            lifecycleScope.launch {
                try {
                    val response = userApi.getCurrentUser()
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            updateWelcomeMessage(user.username)
                        }
                    } else {
                        Log.e("HomeFragment", "Failed to fetch user data: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error fetching user data", e)
                }
            }
        } else {
            Log.w("HomeFragment", "No JWT token found")
        }
    }

    private fun updateWelcomeMessage(username: String) {
        binding.homeTitle.text = "Welcome $username"
    }

    private fun loadCommuteData() {
        lifecycleScope.launch {
            try {
                val response = commuteApi.getMyCommutes()
                if (response.isSuccessful) {
                    response.body()?.let { commutePlans ->
                        displayCommuteData(commutePlans)
                    }
                } else {
                    Log.e("HomeFragment", "Failed to fetch commute data: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching commute data", e)
            }
        }
    }

    private fun displayCommuteData(commutePlans: List<com.example.core.api.CommutePlan>) {
        val dayCommutes = organizeCommutesByDay(commutePlans)
        dailyCommuteAdapter.updateDays(dayCommutes)
        
        // Update subtitle with summary
        val commuteInfo = if (commutePlans.isEmpty()) {
            "No commute plans found"
        } else {
            "Ready for your journey?"
        }
        binding.subTitle.text = commuteInfo
    }

    private fun organizeCommutesByDay(commutePlans: List<com.example.core.api.CommutePlan>): List<DayCommutes> {
        val daysOfWeek = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val today = LocalDate.now()
        
        return (0..6).map { dayOffset ->
            val targetDate = today.plusDays(dayOffset.toLong())
            val dayOfWeekIndex = targetDate.dayOfWeek.value - 1 // Monday = 0
            val shortDayName = daysOfWeek[dayOfWeekIndex]
            
            val dayDisplayName = when (dayOffset) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> dayNames[dayOfWeekIndex]
            }
            
            val dayCommutes = commutePlans.filter { commutePlan ->
                commutePlan.commuteRecurrenceDayIds?.contains(shortDayName) == true
            }
            
            DayCommutes(dayDisplayName, dayCommutes)
        }
    }

    private fun checkForActiveTrip() {
        lifecycleScope.launch {
            try {
                // Get current user first
                val userResponse = userApi.getCurrentUser()
                if (!userResponse.isSuccessful || userResponse.body() == null) {
                    Log.w("HomeFragment", "Failed to get user info for active trip check")
                    return@launch
                }
                
                val username = userResponse.body()!!.username
                val result = tripService.getActiveTrip(username)
                result.fold(
                    onSuccess = { activeTrip ->
                        currentTrip = activeTrip
                        activeTrip?.let { trip ->
                            displayActiveTrip(trip)
                        }
                    },
                    onFailure = { error ->
                        Log.d("HomeFragment", "No active trip found: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error checking active trip", e)
            }
        }
    }

    private fun displayActiveTrip(trip: Trip) {
        googleMapInstance?.let { map ->
            // Clear existing polylines
            routePolylines.forEach { it.remove() }
            routePolylines.clear()

            // Display current leg with highlighted polyline
            val currentLeg = trip.route.legs.getOrNull(trip.currentLegIndex)
            currentLeg?.routePoints?.let { points ->
                if (points.size >= 2) {
                    val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                    
                    // Use different colors for different leg types
                    val color = when (currentLeg.type.uppercase()) {
                        "WALK" -> android.graphics.Color.BLUE
                        "BUS" -> android.graphics.Color.RED
                        else -> android.graphics.Color.GREEN
                    }
                    
                    val polyline = map.addPolyline(
                        PolylineOptions()
                            .addAll(latLngPoints)
                            .color(color)
                            .width(8f)
                    )
                    routePolylines.add(polyline)
                    
                    // Show remaining legs with lighter color
                    trip.route.legs.drop(trip.currentLegIndex + 1).forEach { leg ->
                        leg.routePoints?.let { legPoints ->
                            if (legPoints.size >= 2) {
                                val legLatLngPoints = legPoints.map { LatLng(it.latitude, it.longitude) }
                                val remainingPolyline = map.addPolyline(
                                    PolylineOptions()
                                        .addAll(legLatLngPoints)
                                        .color(android.graphics.Color.GRAY)
                                        .width(4f)
                                )
                                routePolylines.add(remainingPolyline)
                            }
                        }
                    }
                    
                    // Focus camera on current leg
                    val boundsBuilder = LatLngBounds.Builder()
                    latLngPoints.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    
                    try {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error updating camera bounds", e)
                    }
                }
            }
            
            // Show active trip overlay with current leg instruction
            showActiveTripOverlay(trip, currentLeg)
        }
    }

    private fun showActiveTripOverlay(trip: Trip, currentLeg: RouteLeg?) {
        if (!isAdded || _binding == null) return // Prevent crash if view is gone

        // Instruction for current leg
        val instruction = when (currentLeg?.type?.uppercase()) {
            "WALK" -> "Walk to ${currentLeg.toStopName ?: "destination"}"
            "BUS" -> "Take Bus ${currentLeg.busServiceNumber ?: "N/A"} to ${currentLeg.toStopName ?: "destination"}"
            else -> currentLeg?.instruction ?: "Continue your journey"
        }
        binding.tvCurrentInstruction.text = instruction

        // Progress info
        val currentStep = trip.currentLegIndex + 1
        val totalSteps = trip.route.legs.size
        binding.tvLegProgress.text = "Step $currentStep of $totalSteps"

        // Duration
        binding.tvDuration.text = currentLeg?.durationInMinutes?.let { "$it min" } ?: ""

        // Show overlay
        binding.layoutActiveTrip.visibility = View.VISIBLE

        // Subtitle
        binding.subTitle.text = "Ready for your journey?"
    }


    private fun collapseActiveTripOverlay() {
        binding.layoutActiveTrip.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        // Check for trip updates when returning to the fragment
        if (::tripService.isInitialized) {
            checkForActiveTrip()
        }
    }
}