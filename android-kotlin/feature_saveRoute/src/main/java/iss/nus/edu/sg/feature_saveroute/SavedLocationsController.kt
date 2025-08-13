package iss.nus.edu.sg.feature_saveroute

import iss.nus.edu.sg.feature_saveroute.Data.LocationRequest
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedLocationsController @Inject constructor(
    private val commutePlanApi: CommutePlanApi
) {
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