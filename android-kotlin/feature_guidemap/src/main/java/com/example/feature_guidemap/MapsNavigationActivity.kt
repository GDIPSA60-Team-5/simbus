package com.example.feature_guidemap

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private var routePolyline: Polyline? = null

    private lateinit var instructionCard: CardView
    private lateinit var directionIcon: ImageView
    private lateinit var distanceText: TextView
    private lateinit var instructionText: TextView
    private lateinit var timeRemainingText: TextView
    private lateinit var distanceRemainingText: TextView
    private lateinit var speedText: TextView
    private lateinit var audioButton: FloatingActionButton
    private lateinit var endButton: MaterialButton
    private lateinit var compassIcon: ImageView

    private val handler = Handler(Looper.getMainLooper())
    private var navigationRunnable: Runnable? = null
    private var currentInstructionIndex = 0
    private var isAudioEnabled = true

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private val navigationInstructions = listOf(
        NavigationInstruction("Turn right onto Main Street", "200m", TurnDirection.RIGHT, R.drawable.ic_turn_right),
        NavigationInstruction("Continue straight for 1.2km", "1.2km", TurnDirection.STRAIGHT, R.drawable.ic_straight),
        NavigationInstruction("Turn left onto Oak Avenue", "300m", TurnDirection.LEFT, R.drawable.ic_turn_left),
        NavigationInstruction("Slight right onto Highway 101", "450m", TurnDirection.RIGHT, R.drawable.ic_turn_right),
        NavigationInstruction("Take the exit toward Downtown", "800m", TurnDirection.STRAIGHT, R.drawable.ic_straight),
        NavigationInstruction("Turn left onto Broadway", "150m", TurnDirection.LEFT, R.drawable.ic_turn_left),
        NavigationInstruction("Destination on your right", "50m", TurnDirection.DESTINATION, R.drawable.ic_destination)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_navigation)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
        setupAnimations()
        supportActionBar?.hide()

        // Check location permissions
        checkLocationPermissions()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map
        setupMap()

        // Start navigation simulation
        startNavigation()
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

            // Add sample route
            addSampleRoute()
        }

        // Get current location and center map
        getCurrentLocation()
    }

    private fun addSampleRoute() {
        // Sample route points (Singapore area)
        val routePoints = listOf(
            LatLng(1.3521, 103.8198), // Starting point
            LatLng(1.3541, 103.8218),
            LatLng(1.3561, 103.8238),
            LatLng(1.3581, 103.8258),
            LatLng(1.3601, 103.8278)  // Destination
        )

        // Draw route polyline
        routePolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(ContextCompat.getColor(this, R.color.direction_right))
                .width(12f)
                .pattern(listOf(Dash(30f), Gap(20f)))
        )

        // Add destination marker
        googleMap.addMarker(
            MarkerOptions()
                .position(routePoints.last())
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
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
        instructionCard = findViewById(R.id.instruction_card)
        directionIcon = findViewById(R.id.iv_direction_icon)
        distanceText = findViewById(R.id.tv_distance)
        instructionText = findViewById(R.id.tv_instruction)
        timeRemainingText = findViewById(R.id.tv_time_remaining)
        distanceRemainingText = findViewById(R.id.tv_distance_remaining)
        speedText = findViewById(R.id.tv_speed)
        audioButton = findViewById(R.id.fab_audio)
        endButton = findViewById(R.id.btn_end_journey)
        compassIcon = findViewById(R.id.iv_compass)

        // Set click listeners
        audioButton.setOnClickListener { toggleAudio() }
        endButton.setOnClickListener { endJourney() }

        // Initial instruction
        updateInstruction(navigationInstructions[0])
    }

    private fun setupAnimations() {
        // Slide down animation for instruction card
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        instructionCard.startAnimation(slideDown)

        // Slide up animation for bottom controls
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<View>(R.id.bottom_controls).startAnimation(slideUp)

        // Rotate animation for compass
        val compassRotation = ObjectAnimator.ofFloat(compassIcon, "rotation", 0f, 360f)
        compassRotation.duration = 8000
        compassRotation.repeatCount = ValueAnimator.INFINITE
        compassRotation.start()
    }

    private fun startNavigation() {
        navigationRunnable = object : Runnable {
            override fun run() {
                updateNavigation()
                handler.postDelayed(this, 4000) // Update every 4 seconds
            }
        }
        handler.postDelayed(navigationRunnable!!, 4000)
    }

    private fun updateNavigation() {
        currentInstructionIndex = (currentInstructionIndex + 1) % navigationInstructions.size
        val instruction = navigationInstructions[currentInstructionIndex]

        updateInstruction(instruction)
        updateJourneyProgress()
        updateSpeed()
    }

    private fun updateInstruction(instruction: NavigationInstruction) {
        // Fade out current instruction
        instructionCard.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                // Update content
                directionIcon.setImageResource(instruction.iconResId)
                directionIcon.backgroundTintList = ContextCompat.getColorStateList(
                    this@MapsNavigationActivity,
                    getDirectionColor(instruction.direction)
                )
                distanceText.text = instruction.distance
                instructionText.text = instruction.text

                // Fade in with new content
                instructionCard.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun getDirectionColor(direction: TurnDirection): Int {
        return when (direction) {
            TurnDirection.RIGHT -> R.color.direction_right
            TurnDirection.LEFT -> R.color.direction_left
            TurnDirection.STRAIGHT -> R.color.direction_straight
            TurnDirection.DESTINATION -> R.color.direction_destination
        }
    }

    private fun updateJourneyProgress() {
        val timeRemaining = maxOf(1, 12 - currentInstructionIndex * 2)
        val distanceRemaining = maxOf(0.1, 3.2 - currentInstructionIndex * 0.5)

        timeRemainingText.text = "${timeRemaining} min"
        distanceRemainingText.text = String.format("%.1f km remaining", distanceRemaining)
    }

    private fun updateSpeed() {
        val speeds = listOf(42, 45, 38, 50, 35, 48, 41)
        val randomSpeed = speeds.random()
        speedText.text = randomSpeed.toString()
    }

    private fun toggleAudio() {
        isAudioEnabled = !isAudioEnabled

        if (isAudioEnabled) {
            audioButton.setImageResource(R.drawable.ic_volume_up)
            audioButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.audio_enabled)
        } else {
            audioButton.setImageResource(R.drawable.ic_volume_off)
            audioButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.audio_disabled)
        }

        // Add bounce animation
        audioButton.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                audioButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun endJourney() {
        navigationRunnable?.let { handler.removeCallbacks(it) }
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationRunnable?.let { handler.removeCallbacks(it) }
    }
}