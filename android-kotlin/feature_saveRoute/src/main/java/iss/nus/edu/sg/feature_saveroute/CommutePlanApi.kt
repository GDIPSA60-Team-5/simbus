package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.*
import retrofit2.Call
import retrofit2.http.*

interface CommutePlanApi {
    // CommutePlan CRUD (user-scoped)
    @GET("api/user/commute-plans/me")
    fun getSavedCommutePlans(): Call<List<CommutePlanMongo>>

    @POST("api/user/commute-plans")
    fun createCommutePlan(@Body commutePlanData: CommutePlanRequest): Call<CommutePlanMongo>

    @PUT("api/user/commute-plans/{commutePlanId}")
    fun updateCommutePlan(
        @Path("commutePlanId") commutePlanId: String,
        @Body commutePlanData: CommutePlanRequest
    ): Call<CommutePlanMongo>

    @DELETE("api/user/commute-plans/{commutePlanId}")
    fun deleteCommutePlan(@Path("commutePlanId") commutePlanId: String): Call<Void>

    @GET("api/user/commute-plans/{commutePlanId}")
    fun getCommutePlan(@Path("commutePlanId") commutePlanId: String): Call<CommutePlanMongo>

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
    fun getSgBusServices(@Query("busStopCode") busStopCode: String): Call<List<SgBusServiceAtStop>>

    @GET("api/bus/legacy/busServices")
    fun getNusBusServices(@Query("busStopCode") busStopName: String): Call<List<NusBusServiceAtStop>>

    @GET("api/bus/stops/search")
    fun searchBusStops(@Query("query") query: String): Call<List<UnifiedBusStop>>

    // Locations (IDs used in CommutePlan)
    @POST("api/sync/location")
    fun syncLocation(@Body locationData: LocationRequest): Call<SavedLocationMongo>

    @GET("api/locations")
    fun getStoredLocations(): Call<List<SavedLocationMongo>>

    @DELETE("api/locations/{locationId}")
    fun deleteLocation(@Path("locationId") locationId: String): Call<Void>
}