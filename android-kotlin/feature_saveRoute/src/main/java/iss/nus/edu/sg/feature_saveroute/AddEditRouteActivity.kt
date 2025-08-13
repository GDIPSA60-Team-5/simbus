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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.Data.savedLocationData
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import iss.nus.edu.sg.feature_saveroute.databinding.AddeditRouteBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddEditRouteActivity : AppCompatActivity() {
    private lateinit var binding: AddeditRouteBinding
    private var isEdit = false
    private var position = -1

    private lateinit var startAdapter: ArrayAdapter<String>
    private lateinit var endAdapter: ArrayAdapter<String>
    private val ADD_NEW = "+Add new location…"

    @Inject
    lateinit var commutePlanController: CommutePlanController
    @Inject
    lateinit var savedLocationStore: SavedLocationStore

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
            val plan = intent.getParcelableExtra<CommutePlan>("Edit_CommutePlan")
            binding.RoutePageTitle.text = "Edit Commute Plan"
            plan?.let {
                binding.FromEdit.setText(it.startLocationId)
                binding.ToEdit.setText(it.endLocationId)
                binding.BusStopEdit.setText(it.busStop)
                binding.BusServiceEdit.setText(it.busService)
                binding.StartTimeEdit.setText(it.notifyAt)     // notifyAt == “start time”
                binding.ArrivalTimeEdit.setText(it.arrivalTime)

                binding.NotfiEdit.setText((it.notificationNum ?: 0).toString())

                it.selectedDays?.let { days ->
                    if (days.size >= 7) {
                        binding.MonCheck.isChecked = days[0]
                        binding.TuesCheck.isChecked = days[1]
                        binding.WedCheck.isChecked = days[2]
                        binding.ThursCheck.isChecked = days[3]
                        binding.FriCheck.isChecked = days[4]
                        binding.SatCheck.isChecked = days[5]
                        binding.SunCheck.isChecked = days[6]
                    }
                }
                updateSummary()
            }
        }

        binding.RouteSaveButton.setOnClickListener { savePlan() }
        binding.RouteCancelButton.setOnClickListener { finish() }

        binding.BusStopButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopTypeActivity::class.java)
            busStopSelectorLauncher.launch(intent)
        }

        binding.BusServiceButton.setOnClickListener {
            val busStopCodeOrName = binding.BusStopEdit.text.toString().trim()
            if (busStopCodeOrName.isEmpty()) {
                Toast.makeText(this, "Please select a bus stop first", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, SelectBusServiceActivity::class.java)
                intent.putExtra("BusStopCode", busStopCodeOrName)
                busServiceSelectorLauncher.launch(intent)
            }
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

    private fun savePlan() {
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

        val newPlan = CommutePlan(
            startLocationId = binding.FromEdit.text.toString(),
            endLocationId = binding.ToEdit.text.toString(),
            busStop = binding.BusStopEdit.text.toString(),
            busService = binding.BusServiceEdit.text.toString(),
            notifyAt = binding.StartTimeEdit.text.toString(),   // "HH:mm"
            arrivalTime = binding.ArrivalTimeEdit.text.toString(),
            notificationNum = binding.NotfiEdit.text.toString().toIntOrNull() ?: 0,
            recurrence = true,
            selectedDays = frequency
        )

        if (isEdit) {
            val existing = intent.getParcelableExtra<CommutePlan>("Edit_CommutePlan")
            newPlan.id = existing?.id

            val planId = newPlan.id ?: run {
                Toast.makeText(this, "Missing commute plan ID for update", Toast.LENGTH_SHORT).show()
                return
            }

            lifecycleScope.launch {
                commutePlanController.updateCommutePlan(planId, newPlan.toRequest()).fold(
                    onSuccess = {
                        Toast.makeText(this@AddEditRouteActivity, "Plan updated successfully", Toast.LENGTH_SHORT).show()
                        finishWithResult(newPlan)
                    },
                    onFailure = { error ->
                        Toast.makeText(this@AddEditRouteActivity, "Failed to update plan: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            lifecycleScope.launch {
                commutePlanController.createCommutePlan(newPlan.toRequest()).fold(
                    onSuccess = { serverPlan ->
                        newPlan.id = serverPlan.id
                        Toast.makeText(this@AddEditRouteActivity, "Plan saved successfully", Toast.LENGTH_SHORT).show()
                        finishWithResult(newPlan)
                    },
                    onFailure = { error ->
                        Toast.makeText(this@AddEditRouteActivity, "Failed to save plan: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun finishWithResult(plan: CommutePlan) {
        val resultIntent = Intent().apply {
            if (isEdit) putExtra("Edit_CommutePlan", plan)
            else putExtra("New_CommutePlan", plan)
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

    private fun setupLocationAdapters() {
        lifecycleScope.launch {

            val result = commutePlanController.getStoredLocations()
            val saved = result.getOrElse {
                emptyList()
            }

            val display = mutableListOf(ADD_NEW) + saved.map { it.name }

            startAdapter = ArrayAdapter(
                this@AddEditRouteActivity,
                android.R.layout.simple_dropdown_item_1line,
                display
            )
            endAdapter = ArrayAdapter(
                this@AddEditRouteActivity,
                android.R.layout.simple_dropdown_item_1line,
                display
            )

            binding.FromEdit.setAdapter(startAdapter)
            binding.ToEdit.setAdapter(endAdapter)

            fun AutoCompleteTextView.enableDropdown() {
                threshold = 0
                setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) (v as AutoCompleteTextView).showDropDown()
                }
                setOnClickListener { showDropDown() }
            }
            binding.FromEdit.enableDropdown()
            binding.ToEdit.enableDropdown()

            binding.FromEdit.setOnItemClickListener { _, _, position, _ ->
                handleLocationPick(
                    isStart = true,
                    position = position,
                    input = binding.FromEdit,
                    saved = saved.map { savedLocationData(it.name, it.postalCode) }
                )
            }
            binding.ToEdit.setOnItemClickListener { _, _, position, _ ->
                handleLocationPick(
                    isStart = false,
                    position = position,
                    input = binding.ToEdit,
                    saved = saved.map { savedLocationData(it.name, it.postalCode) }
                )
            }
        }
    }

    private fun handleLocationPick(
        isStart: Boolean,
        position: Int,
        input: AutoCompleteTextView,
        saved: List<savedLocationData>
    ) {
        if (position == 0) {
            val intent = Intent(this, CreateSavedLocationActivity::class.java)
                .putExtra("target", if (isStart) "start" else "end")
            addLocationLauncher.launch(intent)
            input.setText("")
            return
        }
        val chosen = saved[position - 1]
        input.setText(chosen.name, false)
    }

    private fun updateSummary() {
        val start = binding.StartTimeEdit.text?.toString()?.trim().orEmpty()
        val notif = binding.NotfiEdit.text?.toString()?.trim().orEmpty()

        if (start.isNotEmpty() && notif.isNotEmpty()) {
            binding.notifupdate.text = getString(R.string.notif_update, start, notif)
            binding.notifupdate.isVisible = true
        } else {
            binding.notifupdate.isGone = true
        }
    }
}