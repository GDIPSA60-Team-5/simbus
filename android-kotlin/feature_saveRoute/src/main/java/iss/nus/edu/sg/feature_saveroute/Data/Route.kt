package iss.nus.edu.sg.feature_saveroute.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Route(
    var from: String? = null,
    var to: String? = null,
    var busStop: String? = null,
    var busService: String? = null,
    var startTime: String? = null,
    var arrivalTime: String? = null,
    var selectedDays: BooleanArray? = null
) : Parcelable