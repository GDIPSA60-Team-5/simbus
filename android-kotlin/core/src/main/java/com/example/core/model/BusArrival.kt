package com.example.core.model

data class BusArrival(
    val serviceName: String,
    val operator: String,
    val arrivals: List<String>
)