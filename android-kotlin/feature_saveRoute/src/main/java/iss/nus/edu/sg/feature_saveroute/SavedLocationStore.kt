package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import iss.nus.edu.sg.feature_saveroute.Data.LocationRequest
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo
import iss.nus.edu.sg.feature_saveroute.Data.savedLocationData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SavedLocationStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commutePlanController: CommutePlanController,
    private val gson: Gson
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    suspend fun load(): MutableList<savedLocationData> = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        try {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<savedLocationData>>() {}.type
            gson.fromJson<MutableList<savedLocationData>>(json, type)
        } catch (_: Exception) {
            prefs.edit().remove(KEY).apply()
            mutableListOf()
        }
    }

    suspend fun save(list: List<savedLocationData>) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    /** Adds locally, then syncs to server via CommutePlanController */
    suspend fun add(item: savedLocationData): Result<SavedLocationMongo> = withContext(Dispatchers.IO) {
        // 1) local persist
        val list = load()
        list.add(item)
        save(list)

        // 2) remote sync - server gets user ID from JWT token automatically
        val body = LocationRequest(name = item.name, postalCode = item.postalCode)
        commutePlanController.syncLocation(body)
    }

    /** Load locations from server and sync with local storage */
    suspend fun loadFromServer(): Result<List<SavedLocationMongo>> = withContext(Dispatchers.IO) {
        commutePlanController.getStoredLocations().fold(
            onSuccess = { serverLocations ->
                // Convert server data to local format and save
                val localData = serverLocations.map {
                    savedLocationData(name = it.name, postalCode = it.postalCode)
                }
                save(localData)
                Result.success(serverLocations)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /** Delete location by ID from server and update local storage */
    suspend fun delete(locationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        commutePlanController.deleteLocation(locationId).fold(
            onSuccess = {
                // Refresh local data from server after successful deletion
                loadFromServer()
                Result.success(Unit)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    companion object {
        private const val PREFS = "app_prefs"
        private const val KEY = "saved_locations_v2"
    }
}