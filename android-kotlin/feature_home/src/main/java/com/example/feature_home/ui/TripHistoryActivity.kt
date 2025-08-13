package com.example.feature_home.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.core.api.UserApi
import com.example.core.service.TripService
import com.example.core.model.Trip
import com.example.feature_home.adapter.TripHistoryAdapter
import com.example.feature_home.databinding.ActivityTripHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TripHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripHistoryBinding
    private lateinit var tripHistoryAdapter: TripHistoryAdapter

    @Inject
    lateinit var tripService: TripService

    @Inject
    lateinit var userApi: UserApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadTripHistory()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        tripHistoryAdapter = TripHistoryAdapter { trip ->
            // Handle trip item click (could navigate to trip details)
            Log.d("TripHistory", "Clicked on trip: ${trip.id}")
        }

        binding.rvTripHistory.apply {
            layoutManager = LinearLayoutManager(this@TripHistoryActivity)
            adapter = tripHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            loadTripHistory()
        }
    }

    private fun loadTripHistory() {
        showLoadingState()

        lifecycleScope.launch {
            try {
                // Get current user first
                val userResponse = userApi.getCurrentUser()
                if (!userResponse.isSuccessful || userResponse.body() == null) {
                    Log.w("TripHistory", "Failed to get user info")
                    showErrorState("Failed to get user information")
                    return@launch
                }

                val username = userResponse.body()!!.username
                val result = tripService.getTripHistory(username)
                
                result.fold(
                    onSuccess = { trips ->
                        if (trips.isEmpty()) {
                            showEmptyState()
                        } else {
                            showTripHistory(trips)
                        }
                    },
                    onFailure = { error ->
                        Log.e("TripHistory", "Error loading trip history", error)
                        showErrorState(error.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e("TripHistory", "Exception loading trip history", e)
                showErrorState("Network error occurred")
            }
        }
    }

    private fun showLoadingState() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvTripHistory.visibility = View.GONE
    }

    private fun showTripHistory(trips: List<Trip>) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvTripHistory.visibility = View.VISIBLE

        tripHistoryAdapter.updateTrips(trips)
    }

    private fun showEmptyState() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.rvTripHistory.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.rvTripHistory.visibility = View.GONE

        binding.tvErrorMessage.text = message
    }
}