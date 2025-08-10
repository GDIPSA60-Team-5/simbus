package iss.nus.edu.sg.feature_saveroute.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Route(
    var id: String? = null,
    var from: String? = null,
    var to: String? = null,
    var busStop: String? = null,
    var busService: String? = null,
    var startTime: String? = null,
    var arrivalTime: String? = null,
    var selectedDays: BooleanArray? = null
) : Parcelable

data class RouteRequest(
    val from: String,
    val to: String,
    val busStop: String,
    val busService: String,
    val startTime: String,
    val arrivalTime: String,
    val selectedDays: List<Boolean>
)

data class RouteMongo(
    val id: String,
    val deviceId: String? = null,
    val from: String,
    val to: String,
    val busStop: String,
    val busService: String,
    val startTime: String,
    val arrivalTime: String? = null,
    val selectedDays: List<Boolean>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun Route.toRequest(): RouteRequest =
    RouteRequest(
        from = from ?: "",
        to = to ?: "",
        busStop = busStop ?: "",
        busService = busService ?: "",
        startTime = startTime ?: "",
        arrivalTime = arrivalTime ?: "",
        selectedDays = (selectedDays ?: booleanArrayOf()).toList()
    )

fun RouteMongo.toUi(): Route =
    Route(
        id = id,
        from = from,
        to = to,
        busStop = busStop,
        busService = busService,
        startTime = startTime,
        arrivalTime = arrivalTime,
        selectedDays = (selectedDays ?: emptyList()).toBooleanArray()
    )

private fun List<Boolean>.toBooleanArray(): BooleanArray {
    val arr = BooleanArray(this.size)
    for (i in indices) arr[i] = this[i]
    return arr
}