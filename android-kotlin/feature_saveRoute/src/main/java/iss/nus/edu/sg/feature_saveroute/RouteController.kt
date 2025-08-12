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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteController @Inject constructor(
    private val routeApi: RouteApi
) {
    // Route operations
    suspend fun getSavedRoutes(deviceId: String): Result<List<RouteMongo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getSavedRoutes(deviceId).execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get routes: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateRoute(deviceId: String, routeId: String, route: RouteRequest): Result<RouteMongo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.updateRoute(deviceId, routeId, route).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to update route: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteRoute(deviceId: String, routeId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.deleteRoute(deviceId, routeId).execute()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete route: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncRoute(deviceId: String, routeData: RouteRequest): Result<RouteMongo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.syncRoute(deviceId, routeData).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to sync route: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Bus stop operations
    suspend fun searchBusStops(query: String): Result<List<UnifiedBusStop>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.searchBusStops(query).execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to search bus stops: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Bus service operations
    suspend fun getSgBusServices(busStopCode: String): Result<List<SgBusServiceAtStop>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getSgBusServices(busStopCode).execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get SG bus services: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getNusBusServices(busStopName: String): Result<List<NusBusServiceAtStop>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getNusBusServices(busStopName).execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get NUS bus services: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Bus arrival operations
    suspend fun getNusArrivalInfo(busStopName: String, serviceName: String): Result<NusBusArrival> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getNusArrivalInfo(busStopName, serviceName).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to get NUS arrival info: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSgBusArrivalInfo(busStopCode: String, serviceNo: String): Result<SgBusArrival> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getSgBusArrivalInfo(busStopCode, serviceNo).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to get SG arrival info: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Location operations
    suspend fun syncLocation(deviceId: String, locationData: LocationRequest): Result<SavedLocationMongo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.syncLocation(deviceId, locationData).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to sync location: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStoredLocations(deviceId: String): Result<List<SavedLocationMongo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = routeApi.getStoredLocations(deviceId).execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get stored locations: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}