package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.RouteMongo
import iss.nus.edu.sg.feature_saveroute.Data.RouteStorage
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.databinding.AddeditRouteBinding
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class AddEditRouteActivity : AppCompatActivity() {
    private lateinit var binding: AddeditRouteBinding
    private var isEdit = false
    private var position = -1

    private lateinit var startAdapter: ArrayAdapter<String>
    private lateinit var endAdapter: ArrayAdapter<String>
    private val ADD_NEW = "+Add new location…"

    private val addLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setupLocationAdapters()

            val target = result.data?.getStringExtra("target")
            val savedName = result.data?.getStringExtra("savedName")

            if (!savedName.isNullOrEmpty()) {
                if (target == "start") {
                    binding.FromEdit.setText(savedName, false)
                    binding.FromEdit.error = null
                } else if (target == "end") {
                    binding.ToEdit.setText(savedName, false)
                    binding.ToEdit.error = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddeditRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationAdapters()

        isEdit = intent.getBooleanExtra("isEdit", false)
        position = intent.getIntExtra("Position", -1)

        if (isEdit) {
            val route = intent.getParcelableExtra<Route>("Edit_Route")
            binding.RoutePageTitle.text = "Edit Route"
            route?.let {
                binding.FromEdit.setText(it.from)
                binding.ToEdit.setText(it.to)
                binding.BusStopEdit.setText(it.busStop)
                binding.BusServiceEdit.setText(it.busService)
                binding.StartTimeEdit.setText(it.startTime)
                binding.ArrivalTimeEdit.setText(it.arrivalTime)
                it.selectedDays?.let { days ->
                    binding.MonCheck.isChecked = days[0]
                    binding.TuesCheck.isChecked = days[1]
                    binding.WedCheck.isChecked = days[2]
                    binding.ThursCheck.isChecked = days[3]
                    binding.FriCheck.isChecked = days[4]
                    binding.SatCheck.isChecked = days[5]
                    binding.SunCheck.isChecked = days[6]
                }
            }
        }

        binding.RouteSaveButton.setOnClickListener {
            saveRoute()
        }

        binding.RouteCancelButton.setOnClickListener { finish() }

        binding.BusStopButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopTypeActivity::class.java)
            busStopSelectorLauncher.launch(intent)
        }

        binding.BusServiceButton.setOnClickListener {
            val busStopCode = binding.BusStopEdit.text.toString().trim()
            if (busStopCode.isEmpty()) {
                Toast.makeText(this, "Please select a bus stop first", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, SelectBusServiceActivity::class.java)
                intent.putExtra("BusStopCode", busStopCode)
                busServiceSelectorLauncher.launch(intent)
            }
        }

        binding.StartTimeEdit.setOnClickListener { showStartTimeClock() }
        binding.ArrivalTimeEdit.setOnClickListener { showArrivalTimeClock() }
    }

    private fun saveRoute() {
        var hasError = false

        if (binding.FromEdit.text.toString().trim().isEmpty()) {
            binding.FromEdit.error = "This field is required"
            hasError = true
        }
        if (binding.ToEdit.text.toString().trim().isEmpty()) {
            binding.ToEdit.error = "This field is required"
            hasError = true
        }
        if (binding.BusStopEdit.text.toString().trim().isEmpty()) {
            binding.BusStopEdit.error = "Please select a bus stop"
            hasError = true
        } else {
            binding.BusStopEdit.error = null
        }
        if (binding.BusServiceEdit.text.toString().trim().isEmpty()) {
            binding.BusServiceEdit.error = "Please select a bus service"
            hasError = true
        }
        if (binding.StartTimeEdit.text.toString().trim().isEmpty()) {
            binding.StartTimeEdit.error = "Please select a start time"
            hasError = true
        } else {
            binding.StartTimeEdit.error = null
        }

        val noDaySelected = !binding.MonCheck.isChecked &&
                !binding.TuesCheck.isChecked &&
                !binding.WedCheck.isChecked &&
                !binding.ThursCheck.isChecked &&
                !binding.FriCheck.isChecked &&
                !binding.SatCheck.isChecked &&
                !binding.SunCheck.isChecked

        if (noDaySelected) {
            binding.frequencyError.visibility = View.VISIBLE
            hasError = true
        } else {
            binding.frequencyError.visibility = View.GONE
        }

        if (hasError) return

        val frequency = booleanArrayOf(
            binding.MonCheck.isChecked,
            binding.TuesCheck.isChecked,
            binding.WedCheck.isChecked,
            binding.ThursCheck.isChecked,
            binding.FriCheck.isChecked,
            binding.SatCheck.isChecked,
            binding.SunCheck.isChecked
        )

        val newRoute = Route(
            from = binding.FromEdit.text.toString(),
            to = binding.ToEdit.text.toString(),
            busStop = binding.BusStopEdit.text.toString(),
            busService = binding.BusServiceEdit.text.toString(),
            startTime = binding.StartTimeEdit.text.toString(),
            arrivalTime = binding.ArrivalTimeEdit.text.toString(),
            selectedDays = frequency
        )

        if (isEdit) {
            val existingRoute = intent.getParcelableExtra<Route>("Edit_Route")
            newRoute.id = existingRoute?.id

            val routeId = newRoute.id ?: run {
                Toast.makeText(this, "Missing route ID for update", Toast.LENGTH_SHORT).show()
                return
            }
            val deviceId = DeviceIdUtil.getDeviceId(this)

            RetrofitClient.api.updateRoute(deviceId, routeId, newRoute.toRequest())
                .enqueue(object : Callback<RouteMongo> {
                    override fun onResponse(call: Call<RouteMongo>, response: Response<RouteMongo>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEditRouteActivity, "Route updated successfully", Toast.LENGTH_SHORT).show()
                            finishWithResult(newRoute)
                        } else {
                            Toast.makeText(this@AddEditRouteActivity, "Failed to update route: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<RouteMongo>, t: Throwable) {
                        Toast.makeText(this@AddEditRouteActivity, "Update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            RouteStorage.syncRouteToMongoDB(this, newRoute) { serverId ->
                if (serverId != null) {
                    newRoute.id = serverId
                    Toast.makeText(this, "Route saved successfully", Toast.LENGTH_SHORT).show()
                    finishWithResult(newRoute)
                } else {
                    Toast.makeText(this, "Failed to save route", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun finishWithResult(route: Route) {
        val resultIntent = Intent().apply {
            if (isEdit) putExtra("Edit_Route", route)
            else putExtra("New_Route", route)
            putExtra("Position", position)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private val busStopSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val busStopCode = result.data?.getStringExtra("BusStopCode")
                if (!busStopCode.isNullOrEmpty()) {
                    val currentBusStopCode = binding.BusStopEdit.text.toString()

                    if (currentBusStopCode != busStopCode) {
                        binding.BusServiceEdit.setText("")
                        binding.BusServiceEdit.error = null
                    }

                    binding.BusStopEdit.setText(busStopCode)
                }
            }
        }

    private val busServiceSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedService = result.data?.getStringExtra("SelectedBusService")
                if (!selectedService.isNullOrEmpty()) {
                    binding.BusServiceEdit.setText(selectedService)
                    binding.BusServiceEdit.error = null
                }
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

    private fun setupLocationAdapters() {
        val saved = SavedLocation.load(this)

        val display = mutableListOf(ADD_NEW) + saved.map { "${it.name}" }

        startAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
        endAdapter   = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)

        binding.FromEdit.setAdapter(startAdapter)
        binding.ToEdit.setAdapter(endAdapter)

        fun AutoCompleteTextView.enableDropdown() {
            threshold = 0
            setOnFocusChangeListener { v, hasFocus -> if (hasFocus) (v as AutoCompleteTextView).showDropDown() }
            setOnClickListener { showDropDown() }
        }
        binding.FromEdit.enableDropdown()
        binding.ToEdit.enableDropdown()

        // handle item picks
        binding.FromEdit.setOnItemClickListener { _, _, position, _ ->
            handleLocationPick(isStart = true, position = position, input = binding.FromEdit)
        }
        binding.ToEdit.setOnItemClickListener { _, _, position, _ ->
            handleLocationPick(isStart = false, position = position, input = binding.ToEdit)
        }





    }

    private fun handleLocationPick(isStart: Boolean, position: Int, input: AutoCompleteTextView) {
        if (position == 0) {
            val intent = Intent(this, CreateSavedLocationActivity::class.java)
                .putExtra("target", if (isStart) "start" else "end")

            addLocationLauncher.launch(intent)
            input.setText("")
            return
        }

        val saved = SavedLocation.load(this)
        val chosen = saved[position - 1]
        input.setText(chosen.name, false)
    }

}