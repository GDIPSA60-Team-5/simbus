package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import iss.nus.edu.sg.feature_saveroute.databinding.AddeditRouteBinding
import com.example.core.api.LocationApi
import com.example.core.api.SavedLocation
import com.example.core.api.RoutingRequest
import com.example.core.api.CommuteApi
import com.example.core.api.CommutePlan
import com.example.core.api.CreateCommutePlanRequest
import com.example.core.api.UpdateCommutePlanRequest
import com.example.core.api.CreatePreferredRouteRequest
import com.example.feature_location.location.AddLocationActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.core.model.Route as CoreRoute

@AndroidEntryPoint
class AddEditRouteActivity : AppCompatActivity() {
    private lateinit var binding: AddeditRouteBinding
    private var isEdit = false
    private var position = -1

    private lateinit var startAdapter: ArrayAdapter<String>
    private lateinit var endAdapter: ArrayAdapter<String>
    private val ADD_NEW = "+Add new location…"
    private var savedLocations = listOf<SavedLocation>()
    private var routeOptions = listOf<CoreRoute>()
    private lateinit var routeOptionsAdapter: RouteOptionsAdapter
    private var selectedRoute: CoreRoute? = null
    private var currentCommutePlan: CommutePlan? = null
    private var selectedRouteId: String? = null

    @Inject
    lateinit var locationApi: LocationApi
    
    @Inject
    lateinit var commuteApi: CommuteApi

    private val addLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload locations to include the new one
            loadUserLocations()

            val target = result.data?.getStringExtra("target")
            val locationName = result.data?.getStringExtra("locationName")

            if (!locationName.isNullOrEmpty()) {
                if (target == "start") {
                    binding.FromEdit.setText(locationName, false)
                    binding.tilFrom.error = null
                } else if (target == "end") {
                    binding.ToEdit.setText(locationName, false)
                    binding.tilTo.error = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddeditRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserLocations()

        isEdit = intent.getBooleanExtra("isEdit", false)
        position = intent.getIntExtra("Position", -1)
        
        // Get commute plan ID if editing
        val commutePlanId = intent.getStringExtra("commutePlanId")
        if (isEdit && !commutePlanId.isNullOrEmpty()) {
            loadCommutePlan(commutePlanId)
        }
        
        setupRouteOptionsRecyclerView()

        if (isEdit) {
            binding.RoutePageTitle.text = "Edit Commute Plan"
            // CommutePlan data will be loaded via loadCommutePlan method
        } else {
            binding.RoutePageTitle.text = "Add Commute Plan"
        }

        binding.RouteSaveButton.setOnClickListener {
            saveRoute()
        }

        binding.RouteCancelButton.setOnClickListener { finish() }

        binding.btnGetRoutes.setOnClickListener {
            getRouteOptions()
        }

        binding.StartTimeEdit.setOnClickListener { showStartTimeClock() }
        binding.ArrivalTimeEdit.setOnClickListener { showArrivalTimeClock() }
        val numbers = (1..10).map { it.toString() }.toTypedArray()
        binding.NotfiEdit.setOnClickListener {
            AlertDialog.Builder(this)
            .setTitle("Select a number")
            .setItems(numbers){_,which->
                binding.NotfiEdit.setText(numbers[which])
                updateSummary()
            }
            .show()
        }


    }

    private fun saveRoute() {
        var hasError = false

        if (binding.FromEdit.text?.toString()?.trim().isNullOrEmpty()) {
            binding.tilFrom.error = "This field is required"
            hasError = true
        } else {
            binding.tilFrom.error = null
        }
        
        if (binding.ToEdit.text?.toString()?.trim().isNullOrEmpty()) {
            binding.tilTo.error = "This field is required"
            hasError = true
        } else {
            binding.tilTo.error = null
        }
        
        if (binding.StartTimeEdit.text?.toString()?.trim().isNullOrEmpty()) {
            binding.tilStartTime.error = "Please select a start time"
            hasError = true
        } else {
            binding.tilStartTime.error = null
        }

        val commutePlanName = "${getSelectedLocation(binding.FromEdit.text?.toString())?.locationName} to ${getSelectedLocation(binding.ToEdit.text?.toString())?.locationName}"
        val fromLocationId = getSelectedLocation(binding.FromEdit.text?.toString())?.id
        val toLocationId = getSelectedLocation(binding.ToEdit.text?.toString())?.id
        
        if (fromLocationId == null || toLocationId == null) {
            Toast.makeText(this, "Please select valid locations", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (hasError) return

        val notifyAt = binding.StartTimeEdit.text?.toString() ?: ""
        val arrivalTime = binding.ArrivalTimeEdit.text?.toString()?.takeIf { it.isNotBlank() }
        val reminderOffset = binding.NotfiEdit.text?.toString()?.toIntOrNull()
        
        // Collect selected recurrence days
        val recurrenceDays = mutableListOf<String>()
        if (binding.MonCheck.isChecked) recurrenceDays.add("mon")
        if (binding.TuesCheck.isChecked) recurrenceDays.add("tue")
        if (binding.WedCheck.isChecked) recurrenceDays.add("wed")
        if (binding.ThursCheck.isChecked) recurrenceDays.add("thu")
        if (binding.FriCheck.isChecked) recurrenceDays.add("fri")
        if (binding.SatCheck.isChecked) recurrenceDays.add("sat")
        if (binding.SunCheck.isChecked) recurrenceDays.add("sun")
        
        val hasRecurrence = recurrenceDays.isNotEmpty()

        lifecycleScope.launch {
            try {
                if (isEdit && currentCommutePlan != null) {
                    val updateRequest = UpdateCommutePlanRequest(
                        commutePlanName = commutePlanName,
                        notifyAt = notifyAt,
                        arrivalTime = arrivalTime,
                        reminderOffsetMin = reminderOffset,
                        recurrence = hasRecurrence,
                        startLocationId = fromLocationId,
                        endLocationId = toLocationId,
                        commuteRecurrenceDayIds = if (hasRecurrence) recurrenceDays else null
                    )
                    
                    val response = commuteApi.updateCommutePlan(currentCommutePlan!!.id, updateRequest)
                    if (response.isSuccessful) {
                        val updatedPlan = response.body()!!
                        
                        // Save preferred route if selected
                        if (selectedRouteId != null) {
                            try {
                                commuteApi.addPreferredRoute(
                                    updatedPlan.id,
                                    CreatePreferredRouteRequest(selectedRouteId!!)
                                )
                            } catch (e: Exception) {
                                // Log but don't fail - the commute plan was saved successfully
                            }
                        }
                        
                        Toast.makeText(this@AddEditRouteActivity, "Commute plan updated successfully", Toast.LENGTH_SHORT).show()
                        finishWithCommutePlan(updatedPlan)
                    } else {
                        Toast.makeText(this@AddEditRouteActivity, "Failed to update commute plan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val createRequest = CreateCommutePlanRequest(
                        commutePlanName = commutePlanName,
                        notifyAt = notifyAt,
                        arrivalTime = arrivalTime,
                        reminderOffsetMin = reminderOffset,
                        recurrence = hasRecurrence,
                        startLocationId = fromLocationId!!,
                        endLocationId = toLocationId!!,
                        commuteRecurrenceDayIds = if (hasRecurrence) recurrenceDays else null
                    )
                    
                    val response = commuteApi.createCommutePlan(createRequest)
                    if (response.isSuccessful) {
                        val newPlan = response.body()!!
                        
                        // Save preferred route if selected
                        if (selectedRouteId != null) {
                            try {
                                commuteApi.addPreferredRoute(
                                    newPlan.id,
                                    CreatePreferredRouteRequest(selectedRouteId!!)
                                )
                            } catch (e: Exception) {
                                // Log but don't fail - the commute plan was saved successfully
                            }
                        }
                        
                        Toast.makeText(this@AddEditRouteActivity, "Commute plan created successfully", Toast.LENGTH_SHORT).show()
                        finishWithCommutePlan(newPlan)
                    } else {
                        Toast.makeText(this@AddEditRouteActivity, "Failed to create commute plan", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRouteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun finishWithCommutePlan(commutePlan: CommutePlan) {
        val resultIntent = Intent().apply {
            if (isEdit) putExtra("Updated_CommutePlan", commutePlan)
            else putExtra("New_CommutePlan", commutePlan)
            putExtra("Position", position)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    
    private fun loadCommutePlan(commutePlanId: String) {
        lifecycleScope.launch {
            try {
                val response = commuteApi.getCommutePlan(commutePlanId)
                if (response.isSuccessful && response.body() != null) {
                    currentCommutePlan = response.body()!!
                    populateFormFromCommutePlan(currentCommutePlan!!)
                } else {
                    Toast.makeText(this@AddEditRouteActivity, "Failed to load commute plan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRouteActivity, "Error loading commute plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun populateFormFromCommutePlan(commutePlan: CommutePlan) {
        lifecycleScope.launch {
            // Load location names from IDs
            val startLocation = commutePlan.startLocationId?.let { id ->
                savedLocations.find { it.id == id }
            }
            val endLocation = commutePlan.endLocationId?.let { id ->
                savedLocations.find { it.id == id }
            }
            
            binding.FromEdit.setText(startLocation?.locationName ?: "")
            binding.ToEdit.setText(endLocation?.locationName ?: "")
            binding.StartTimeEdit.setText(commutePlan.notifyAt)
            binding.ArrivalTimeEdit.setText(commutePlan.arrivalTime ?: "")
            binding.NotfiEdit.setText(commutePlan.reminderOffsetMin?.toString() ?: "")
            
            // Set recurrence day checkboxes
            commutePlan.commuteRecurrenceDayIds?.let { days ->
                binding.MonCheck.isChecked = days.contains("mon")
                binding.TuesCheck.isChecked = days.contains("tue")
                binding.WedCheck.isChecked = days.contains("wed")
                binding.ThursCheck.isChecked = days.contains("thu")
                binding.FriCheck.isChecked = days.contains("fri")
                binding.SatCheck.isChecked = days.contains("sat")
                binding.SunCheck.isChecked = days.contains("sun")
            }
            
            updateSummary()
        }
    }


    private fun showStartTimeClock() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12).setMinute(0)
            .setTitleText("Select Start Time").build()

        picker.show(supportFragmentManager, "timePicker")
        picker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
            binding.StartTimeEdit.setText(formattedTime)
            updateSummary()
        }
    }

    private fun showArrivalTimeClock() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12).setMinute(0)
            .setTitleText("Select Arrival Time").build()

        picker.show(supportFragmentManager, "arrivalTimePicker")
        picker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
            binding.ArrivalTimeEdit.setText(formattedTime)
        }
    }

    private fun loadUserLocations() {
        lifecycleScope.launch {
            try {
                val response = locationApi.getUserLocations()
                if (response.isSuccessful && response.body() != null) {
                    savedLocations = response.body()!!
                    setupLocationAdapters()
                } else {
                    Toast.makeText(this@AddEditRouteActivity, "Failed to load locations", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRouteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLocationAdapters() {
        val display = mutableListOf(ADD_NEW) + savedLocations.map { it.locationName }

        startAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
        endAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)

        binding.FromEdit.setAdapter(startAdapter)
        binding.ToEdit.setAdapter(endAdapter)

        fun AutoCompleteTextView.enableDropdown() {
            threshold = 0
            setOnFocusChangeListener { v, hasFocus -> if (hasFocus) (v as AutoCompleteTextView).showDropDown() }
            setOnClickListener { showDropDown() }
        }
        binding.FromEdit.enableDropdown()
        binding.ToEdit.enableDropdown()

        binding.FromEdit.setOnItemClickListener { _, _, position, _ ->
            handleLocationPick(isStart = true, position = position, input = binding.FromEdit)
        }
        binding.ToEdit.setOnItemClickListener { _, _, position, _ ->
            handleLocationPick(isStart = false, position = position, input = binding.ToEdit)
        }
    }

    private fun setupRouteOptionsRecyclerView() {
        routeOptionsAdapter = RouteOptionsAdapter { route ->
            selectedRoute = route
            selectedRouteId = "route_${route.hashCode()}" // Generate a unique ID
            binding.selectedRouteCard.isVisible = true
            binding.routeDuration.text = "${route.durationInMinutes} min"
            binding.routeSummary.text = route.summary
        }
        
        binding.recyclerRouteOptions.apply {
            layoutManager = LinearLayoutManager(this@AddEditRouteActivity)
            adapter = routeOptionsAdapter
        }
    }

    private fun getRouteOptions() {
        val fromLocation = getSelectedLocation(binding.FromEdit.text?.toString())
        val toLocation = getSelectedLocation(binding.ToEdit.text?.toString())
        
        if (fromLocation == null || toLocation == null) {
            Toast.makeText(this, "Please select both start and end locations", Toast.LENGTH_SHORT).show()
            return
        }
        
        val startTime = binding.StartTimeEdit.text?.toString()?.takeIf { it.isNotBlank() }
        val arrivalTime = binding.ArrivalTimeEdit.text?.toString()?.takeIf { it.isNotBlank() }
        
        val routingRequest = RoutingRequest(
            startCoordinates = "${fromLocation.latitude},${fromLocation.longitude}",
            endCoordinates = "${toLocation.latitude},${toLocation.longitude}",
            startLocation = fromLocation.locationName,
            endLocation = toLocation.locationName,
            startTime = startTime,
            arrivalTime = arrivalTime
        )
        
        lifecycleScope.launch {
            try {
                binding.btnGetRoutes.isEnabled = false
                binding.btnGetRoutes.text = "Loading..."
                
                val response = locationApi.getRouteOptions(routingRequest)
                if (response.isSuccessful && response.body() != null) {
                    val directionsResponse = response.body()!!
                    routeOptions = directionsResponse.suggestedRoutes
                    routeOptionsAdapter.updateRoutes(routeOptions)
                    binding.routeOptionsContainer.isVisible = true
                    
                    if (routeOptions.isEmpty()) {
                        Toast.makeText(this@AddEditRouteActivity, "No routes found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AddEditRouteActivity, "Failed to get routes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRouteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnGetRoutes.isEnabled = true
                binding.btnGetRoutes.text = "Get Route Options"
            }
        }
    }
    
    private fun getSelectedLocation(locationName: String?): SavedLocation? {
        return if (locationName.isNullOrBlank()) null else savedLocations.find { it.locationName == locationName }
    }

    private fun handleLocationPick(isStart: Boolean, position: Int, input: AutoCompleteTextView) {
        if (position == 0) {
            // Navigate to add location screen
            val intent = Intent(this, com.example.feature_location.location.AddLocationActivity::class.java).apply {
                putExtra("target", if (isStart) "start" else "end")
            }
            addLocationLauncher.launch(intent)
            input.setText("")
            return
        }

        val chosen = savedLocations[position - 1]
        input.setText(chosen.locationName, false)
        
        // Clear route options when location changes
        binding.routeOptionsContainer.isGone = true
        binding.selectedRouteCard.isGone = true
        selectedRoute = null
    }

    private fun updateSummary() {
        val start = binding.StartTimeEdit.text?.toString()?.trim().orEmpty()
        val notif = binding.NotfiEdit.text?.toString()?.trim().orEmpty()

        if (start.isNotEmpty() && notif.isNotEmpty()) {
            binding.notifupdate.text = "Notification: $notif times, starting at $start"
            binding.notifupdate.isVisible = true
        } else {
            binding.notifupdate.isGone = true
        }
    }

}