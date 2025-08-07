package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.NusBusArrival
import iss.nus.edu.sg.feature_saveroute.Data.NusBusServiceAtStop
import iss.nus.edu.sg.feature_saveroute.Data.SgBusArrival
import iss.nus.edu.sg.feature_saveroute.Data.SgBusServiceAtStop
import iss.nus.edu.sg.feature_saveroute.Data.UnifiedBusStop
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("api/bus/legacy/nusbus")
    fun getNusArrivalInfo(
        @Query("busStopName") busStopName: String,
        @Query("serviceName") serviceName: String
    ): Call<NusBusArrival>

    @GET("api/bus/legacy/sgbus")
    fun getSgBusArrivalInfo(
        @Query("busStopCode") busStopCode: String,
        @Query("busNumber") serviceNo: String
    ): Call<SgBusArrival>

    @GET("api/bus/legacy/busServices")
    fun getSgBusServices(
        @Query("busStopCode") busStopCode: String
    ): Call<List<SgBusServiceAtStop>>

    @GET("api/bus/legacy/busServices")
    fun getNusBusServices(
        @Query("busStopCode") busStopName: String
    ): Call<List<NusBusServiceAtStop>>

    @GET("api/bus/stops/search")
    fun searchBusStops(
        @Query("query") query: String
    ): Call<List<UnifiedBusStop>>
}