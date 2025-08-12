package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.Data.RouteRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteStorage @Inject constructor(
    private val routeController: RouteController,
    @ApplicationContext private val context: Context
) {

    fun syncRouteToMongoDB(route: Route, callback: ((String?) -> Unit)? = null) {
        val deviceId = DeviceIdUtil.getDeviceId(context)
        val routeRequest = RouteRequest(
            from = route.from ?: "",
            to = route.to ?: "",
            busStop = route.busStop ?: "",
            busService = route.busService ?: "",
            startTime = route.startTime ?: "",
            arrivalTime = route.arrivalTime ?: "",
            notificationNum = route.notificationNum?: "",
            selectedDays = (route.selectedDays ?: booleanArrayOf()).toList()
        )

        CoroutineScope(Dispatchers.IO).launch {
            routeController.syncRoute(deviceId, routeRequest).fold(
                onSuccess = { serverRoute ->
                    Log.d("MongoDB", "Route synced with id: ${serverRoute.id}")
                    callback?.invoke(serverRoute.id)
                },
                onFailure = { error ->
                    Log.e("MongoDB", "Route sync failed: ${error.message}")
                    callback?.invoke(null)
                }
            )
        }
    }
}