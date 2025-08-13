package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommutePlanController @Inject constructor(
    private val commutePlanApi: CommutePlanApi
) {
    // CommutePlan operations
    suspend fun getSavedCommutePlans(): Result<List<CommutePlanMongo>> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.getSavedCommutePlans().execute()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Failed to get commute plans: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCommutePlan(commutePlanData: CommutePlanRequest): Result<CommutePlanMongo> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.createCommutePlan(commutePlanData).execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to create commute plan: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateCommutePlan(commutePlanId: String, commutePlan: CommutePlanRequest): Result<CommutePlanMongo> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.updateCommutePlan(commutePlanId, commutePlan).execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to update commute plan: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteCommutePlan(commutePlanId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.deleteCommutePlan(commutePlanId).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to delete commute plan: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommutePlan(commutePlanId: String): Result<CommutePlanMongo> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.getCommutePlan(commutePlanId).execute()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get commute plan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bus stops search
    suspend fun searchBusStops(query: String): Result<List<UnifiedBusStop>> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.searchBusStops(query).execute()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Failed to search bus stops: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bus services
    suspend fun getSgBusServices(busStopCode: String): Result<List<SgBusServiceAtStop>> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.getSgBusServices(busStopCode).execute()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Failed to get SG bus services: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNusBusServices(busStopName: String): Result<List<NusBusServiceAtStop>> = withContext(Dispatchers.IO) {
        try {
            val response = commutePlanApi.getNusBusServices(busStopName).execute()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Failed to get NUS bus services: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Arrivals
    suspend fun getNusArrivalInfo(busStopName: String, serviceName: String): Result<NusBusArrival> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.getNusArrivalInfo(busStopName, serviceName).execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to get NUS arrival info: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSgBusArrivalInfo(busStopCode: String, serviceNo: String): Result<SgBusArrival> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.getSgBusArrivalInfo(busStopCode, serviceNo).execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to get SG arrival info: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Locations (IDs used in CommutePlan)
    suspend fun syncLocation(locationData: LocationRequest): Result<SavedLocationMongo> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.syncLocation(locationData).execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Failed to sync location: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStoredLocations(): Result<List<SavedLocationMongo>> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.getStoredLocations().execute()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get stored locations: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteLocation(locationId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = commutePlanApi.deleteLocation(locationId).execute()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete location: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}