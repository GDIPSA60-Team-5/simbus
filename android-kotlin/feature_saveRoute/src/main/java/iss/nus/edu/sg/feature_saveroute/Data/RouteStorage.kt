package iss.nus.edu.sg.feature_saveroute.Data

import android.content.Context
import android.util.Log
import iss.nus.edu.sg.feature_saveroute.DeviceIdUtil
import iss.nus.edu.sg.feature_saveroute.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RouteStorage {

    fun syncRouteToMongoDB(context: Context, route: Route, callback: ((String?) -> Unit)? = null) {
        val deviceId = DeviceIdUtil.getDeviceId(context)
        val routeRequest = RouteRequest(
            from = route.from ?: "",
            to = route.to ?: "",
            busStop = route.busStop ?: "",
            busService = route.busService ?: "",
            startTime = route.startTime ?: "",
            arrivalTime = route.arrivalTime ?: "",
            selectedDays = (route.selectedDays ?: booleanArrayOf()).toList()
        )

        RetrofitClient.api.syncRoute(deviceId, routeRequest).enqueue(object : Callback<RouteMongo> {
            override fun onResponse(call: Call<RouteMongo>, response: Response<RouteMongo>) {
                if (response.isSuccessful && response.body() != null) {
                    val serverRoute = response.body()!!
                    Log.d("MongoDB", "Route synced with id: ${serverRoute.id}")
                    callback?.invoke(serverRoute.id)
                } else {
                    Log.e("MongoDB", "Route sync failed with code: ${response.code()}")
                    callback?.invoke(null)
                }
            }
            override fun onFailure(call: Call<RouteMongo>, t: Throwable) {
                Log.e("MongoDB", "Route sync failed: ${t.message}")
                callback?.invoke(null)
            }
        })
    }
}