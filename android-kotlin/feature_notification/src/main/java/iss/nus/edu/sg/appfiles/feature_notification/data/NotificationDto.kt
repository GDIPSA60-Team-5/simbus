package iss.nus.edu.sg.appfiles.feature_notification.data

data class BusArrival(
    val busService: String,   // e.g., "980"
    val arrivalTime: String   // e.g., "12:34" or ISO time
)

data class NotificationDto(
    val title: String,
    val body: String,
    val nextBuses: List<BusArrival>  // typically size 2 for next two buses
)
