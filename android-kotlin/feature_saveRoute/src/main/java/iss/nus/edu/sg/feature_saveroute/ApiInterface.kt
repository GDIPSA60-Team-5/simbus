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
    @GET("api/legacy/nusbus")
    fun getNusArrivalInfo(
        @Query("busStopName") busStopName: String,
        @Query("busService") serviceName: String
    ): Call<NusBusArrival>

    @GET("api/legacy/sgbus")
    fun getSgBusArrivalInfo(
        @Query("busStopCode") busStopCode: String,
        @Query("busNumber") serviceNo: String
    ) : Call<SgBusArrival>

    @GET("api/legacy/busServices")
    fun getSgBusServices(
        @Query("busStopCode") busStopCode: String
    ): Call<List<SgBusServiceAtStop>>

    @GET("api/legacy/busServices")
    fun getNusBusServices(
        @Query("busStopCode") busStopName: String
    ): Call<List<NusBusServiceAtStop>>

    @GET("api/bus/stops/search")
    fun searchBusStops(
        @Query("query") query: String
    ): Call<List<UnifiedBusStop>>

    @GET("api/bus/stops/search")
    fun getSgBusStops(): Call<List<UnifiedBusStop>>

    @GET("api/bus/stops/search")
    fun getNusBusStops(): Call<List<UnifiedBusStop>>
}
