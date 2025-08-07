package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.databinding.AddeditRouteBinding

class AddEditRouteActivity : AppCompatActivity() {
    private lateinit var binding: AddeditRouteBinding
    private var isEdit = false
    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddeditRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        }
        if (binding.BusServiceEdit.text.toString().trim().isEmpty()) {
            binding.BusServiceEdit.error = "Please select a bus service"
            hasError = true
        }
        if (binding.StartTimeEdit.text.toString().trim().isEmpty()) {
            binding.StartTimeEdit.error = "Please select a start time"
            hasError = true
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

        val resultIntent = Intent().apply {
            if (isEdit) putExtra("Edit_Route", newRoute)
            else putExtra("New_Route", newRoute)
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
}