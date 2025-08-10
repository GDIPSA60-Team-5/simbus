package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.util.Log
import iss.nus.edu.sg.feature_saveroute.Data.LocationRequest
import iss.nus.edu.sg.feature_saveroute.Data.SavedLocationMongo
import iss.nus.edu.sg.feature_saveroute.Data.savedLocationData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SavedLocation {
    private const val PREFS = "app_prefs"
    private const val KEY = "saved_locations_v2"

    fun load(context: Context): MutableList<savedLocationData> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<savedLocationData>>() {}.type
            com.google.gson.Gson().fromJson(json, type)
        } catch (e: Exception) {
            prefs.edit().remove(KEY).apply()
            mutableListOf()
        }
    }

    fun save(context: Context, list: List<savedLocationData>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, com.google.gson.Gson().toJson(list)).apply()
    }

    fun add(context: Context, item: savedLocationData) {
        val list = load(context)
        list.add(item)
        save(context, list)

        val deviceId = DeviceIdUtil.getDeviceId(context)
        val locationRequest = LocationRequest(name = item.name, postalCode = item.postalCode)

        RetrofitClient.api.syncLocation(deviceId, locationRequest).enqueue(object : Callback<SavedLocationMongo> {
            override fun onResponse(call: Call<SavedLocationMongo>, response: Response<SavedLocationMongo>) {
                if (response.isSuccessful) {
                    Log.d("MongoDB", "Location synced to database: ${response.body()}")
                } else {
                    Log.e("MongoDB", "Sync failed with code: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<SavedLocationMongo>, t: Throwable) {
                Log.e("MongoDB", "Sync failed: ${t.message}")
            }
        })
    }

}