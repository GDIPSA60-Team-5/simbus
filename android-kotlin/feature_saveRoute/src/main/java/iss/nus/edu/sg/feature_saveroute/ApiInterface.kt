package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.LocationRequest
import iss.nus.edu.sg.feature_saveroute.Data.NusBusArrival
import iss.nus.edu.sg.feature_saveroute.Data.NusBusServiceAtStop
import iss.nus.edu.sg.feature_saveroute.Data.RouteMongo
import iss.nus.edu.sg.feature_saveroute.Data.RouteRequest
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo
import iss.nus.edu.sg.feature_saveroute.Data.SgBusArrival
import iss.nus.edu.sg.feature_saveroute.Data.SgBusServiceAtStop
import iss.nus.edu.sg.feature_saveroute.Data.UnifiedBusStop
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @POST("api/bus/sync/location")
    fun syncLocation(@Header("Device-ID") deviceId: String, @Body locationData: LocationRequest): Call<SavedLocationMongo>

    @POST("api/bus/sync/route")
    fun syncRoute(@Header("Device-ID") deviceId: String, @Body routeData: RouteRequest): Call<RouteMongo>

    @GET("api/bus/locations")
    fun getStoredLocations(@Header("Device-ID") deviceId: String): Call<List<SavedLocationMongo>>

    @GET("api/bus/routes")
    fun getSavedRoutes(
        @Header("Device-ID") deviceId: String): retrofit2.Call<List<RouteMongo>>

    @PUT("api/bus/routes/{routeId}")
    fun updateRoute(
        @Header("Device-ID") deviceId: String,
        @Path("routeId") routeId: String,
        @Body route: RouteRequest
    ): retrofit2.Call<RouteMongo>

    @DELETE("api/bus/routes/{routeId}")
    fun deleteRoute(
        @Header("Device-ID") deviceId: String,
        @Path("routeId") routeId: String
    ): retrofit2.Call<Void>
}