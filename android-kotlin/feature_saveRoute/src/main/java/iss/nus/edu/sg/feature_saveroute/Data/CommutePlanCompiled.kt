package iss.nus.edu.sg.feature_saveroute.Data

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommutePlan(
    var id: String? = null,
    var userId: String? = null,
    var commutePlanName: String? = null,
    var notifyAt: String? = null,      // "HH:mm"
    var arrivalTime: String? = null,   // "HH:mm"
    var notificationNum: Int? = null,
    var recurrence: Boolean? = null,   // true if repeating
    var startLocationId: String? = null, // references Location.id
    var endLocationId: String? = null,   // references Location.id
    var busStop: String? = null,       // UI: stop code/name
    var busService: String? = null,    // UI: service number/name
    var selectedDays: BooleanArray? = null // length 7 (Mon..Sun)
) : Parcelable {
    // Override equals and hashCode to handle BooleanArray properly
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommutePlan

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (commutePlanName != other.commutePlanName) return false
        if (notifyAt != other.notifyAt) return false
        if (arrivalTime != other.arrivalTime) return false
        if (notificationNum != other.notificationNum) return false
        if (recurrence != other.recurrence) return false
        if (startLocationId != other.startLocationId) return false
        if (endLocationId != other.endLocationId) return false
        if (busStop != other.busStop) return false
        if (busService != other.busService) return false
        if (selectedDays != null) {
            if (other.selectedDays == null) return false
            if (!selectedDays.contentEquals(other.selectedDays)) return false
        } else if (other.selectedDays != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (commutePlanName?.hashCode() ?: 0)
        result = 31 * result + (notifyAt?.hashCode() ?: 0)
        result = 31 * result + (arrivalTime?.hashCode() ?: 0)
        result = 31 * result + (notificationNum ?: 0)
        result = 31 * result + (recurrence?.hashCode() ?: 0)
        result = 31 * result + (startLocationId?.hashCode() ?: 0)
        result = 31 * result + (endLocationId?.hashCode() ?: 0)
        result = 31 * result + (busStop?.hashCode() ?: 0)
        result = 31 * result + (busService?.hashCode() ?: 0)
        result = 31 * result + (selectedDays?.contentHashCode() ?: 0)
        return result
    }
}

data class CommutePlanRequest(
    val commutePlanName: String,
    val notifyAt: String,              // "HH:mm"
    val arrivalTime: String,           // "HH:mm"
    val notificationNum: Int,
    val recurrence: Boolean,
    val startLocationId: String,
    val endLocationId: String,
    val busStopCode: String? = null,
    val busServiceNo: String? = null,
    val selectedDays: List<Boolean>? = null
)

data class CommutePlanMongo(
    val id: String,
    val userId: String? = null,
    val commutePlanName: String,
    val notifyAt: String,              // "HH:mm"
    val arrivalTime: String,           // "HH:mm"
    val notificationNum: Int,
    val recurrence: Boolean,
    val startLocationId: String,
    val endLocationId: String,
    val busStopCode: String? = null,
    val busServiceNo: String? = null,
    val selectedDays: List<Boolean>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun CommutePlan.toRequest(): CommutePlanRequest {
    val selectedDaysList = selectedDays?.let { days ->
        // Ensure we always send exactly 7 days
        val list = days.toList()
        if (list.size < 7) {
            list + List(7 - list.size) { false }
        } else {
            list.take(7)
        }
    }

    Log.d("DataConversion", "Converting UI to Request: ${selectedDays?.contentToString()} -> $selectedDaysList")

    return CommutePlanRequest(
        commutePlanName = (commutePlanName ?: "").ifBlank { "${startLocationId ?: "-"} → ${endLocationId ?: "-"}" },
        notifyAt = notifyAt ?: "",
        arrivalTime = arrivalTime ?: "",
        notificationNum = notificationNum ?: 0,
        recurrence = recurrence ?: false,
        startLocationId = startLocationId ?: "",
        endLocationId = endLocationId ?: "",
        busStopCode = busStop?.takeIf { it.isNotBlank() },
        busServiceNo = busService?.takeIf { it.isNotBlank() },
        selectedDays = selectedDaysList
    )
}

fun CommutePlanMongo.toUi(): CommutePlan {
    val selectedDaysArray = selectedDays?.let { daysList ->
        // Ensure we always have exactly 7 days, pad with false if needed
        val normalizedList = if (daysList.size < 7) {
            daysList + List(7 - daysList.size) { false }
        } else {
            daysList.take(7)
        }
        normalizedList.toBooleanArray()
    }

    Log.d("DataConversion", "Converting Mongo to UI: $selectedDays -> ${selectedDaysArray?.contentToString()}")

    return CommutePlan(
        id = id,
        userId = userId,
        commutePlanName = commutePlanName,
        notifyAt = notifyAt,
        arrivalTime = arrivalTime,
        notificationNum = notificationNum,
        recurrence = recurrence,
        startLocationId = startLocationId,
        endLocationId = endLocationId,
        busStop = busStopCode,
        busService = busServiceNo,
        selectedDays = selectedDaysArray
    )
}

private fun List<Boolean>.toBooleanArray(): BooleanArray {
    return BooleanArray(this.size) { i -> this[i] }
}