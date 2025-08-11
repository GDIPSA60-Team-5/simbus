package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.NusBusStop
import iss.nus.edu.sg.feature_saveroute.Data.SgBusStop
import iss.nus.edu.sg.feature_saveroute.Data.UnifiedBusStop

object BusStopCache {
    var sgBusStops: List<SgBusStop>? = null
    var nusBusStops: List<NusBusStop>? = null

    var allBusStops: List<UnifiedBusStop>? = null

    fun getSgBusStopsFromUnified(): List<SgBusStop> {
        return allBusStops?.filter { it.sourceApi == "LTA" }?.map { unified ->
            SgBusStop().apply {
                busStopCode = unified.code
                description = unified.name
                latitude = unified.latitude.toString()
                longitude = unified.longitude.toString()
            }
        } ?: emptyList()
    }

    fun getNusBusStopsFromUnified(): List<NusBusStop> {
        return allBusStops?.filter { it.sourceApi == "NUS" }?.map { unified ->
            NusBusStop().apply {
                name = unified.code
                longName = unified.name
                latitude = unified.latitude
                longitude = unified.longitude
            }
        } ?: emptyList()
    }
}