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
import com.example.feature_home.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import com.example.feature_guidemap.MapsNavigationActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.core.api.UserApi
import com.example.core.api.CommuteApi
import com.example.feature_chatbot.ui.ChatbotActivity
import com.example.feature_home.adapter.DailyCommuteAdapter
import com.example.feature_home.adapter.DayCommutes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMapInstance: GoogleMap? = null
    private lateinit var dailyCommuteAdapter: DailyCommuteAdapter

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
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@HomeFragment)
        }
    }

    private fun setupCommuteViewPager() {
        dailyCommuteAdapter = DailyCommuteAdapter()
        binding.viewPager.adapter = dailyCommuteAdapter
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
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
}