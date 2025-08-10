package com.example.feature_home.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.feature_home.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMapInstance: GoogleMap? = null

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
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        setupWindowInsets()
        setupLocationClient()
        setupMapView(savedInstanceState)
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

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@HomeFragment)
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun hasLocationPermission(): Boolean =
        LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMapInstance = googleMap
        handleLocationPermissionAndSetup()
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
}