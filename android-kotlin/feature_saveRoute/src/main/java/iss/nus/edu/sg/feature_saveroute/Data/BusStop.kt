package iss.nus.edu.sg.feature_saveroute.Data

data class BusStop(
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val sourceApi: String
) {
    fun getDisplayName(): String = "$name ($sourceApi)"

    fun getIdentifier(): String = if (sourceApi == "LTA") code else name
}

data class BusArrival(
    val serviceName: String,
    val operator: String,
    val arrivals: List<String>
)

data class UnifiedBusStop(
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val sourceApi: String
)

data class SgBusStop(
    var busStopCode: String? = null,
    var roadName: String? = null,
    var description: String? = null,
    var latitude: String? = null,
    var longitude: String? = null
) {
    constructor() : this(null, null, null, null, null)
}

data class NusBusStop(
    var name: String? = null,
    var longName: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) {
    constructor() : this(null, null, 0.0, 0.0)
}

data class SgBusArrival(
    val serviceNo: String? = null,
    val estimatedArrivals: List<String>? = null
)

data class NusBusArrival(
    val serviceName: String? = null,
    val arrivalTime: String? = null,
    val nextArrivalTime: String? = null
)

data class SgBusServiceAtStop(
    val serviceNo: String? = null
)

data class NusBusServiceAtStop(
    val name: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NusBusServiceAtStop
        return name == other.name
    }

    override fun hashCode(): Int {
        return name?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "NusBusServiceAtStop(name='$name')"
    }
}