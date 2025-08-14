package com.example.core.api

import com.example.core.model.BusArrival
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApi {
    @GET("api/bus/arrivals")
    suspend fun getBusArrivals(
        @Query("busStopQuery") busStopQuery: String,
        @Query("serviceNo") serviceNo: String? = null
    ): Response<List<BusArrival>>
}