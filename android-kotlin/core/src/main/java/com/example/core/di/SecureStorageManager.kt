package com.example.core.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

@Suppress("DEPRECATION")
class SecureStorageManager(context: Context) {

    companion object {
        private const val PREF_NAME = "secure_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    private val sharedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        val expiryMillis = decodeJwtExpiry(token)
        sharedPrefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRY, expiryMillis)
            .apply()
    }

    fun getToken(): String? {
        val expiry = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        if (System.currentTimeMillis() > expiry) {
            return null
        }
        return sharedPrefs.getString(KEY_TOKEN, null)
    }

    fun getTokenExpiry(): Long {
        return sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }

    fun saveUsername(username: String) {
        sharedPrefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPrefs.getString(KEY_USERNAME, null)
    }

    fun clearAll() {
        sharedPrefs.edit().clear().apply()
    }

    private fun decodeJwtExpiry(token: String): Long {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return 0
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val expSeconds = JSONObject(payload).optLong("exp", 0)
            expSeconds * 1000 // convert to ms
        } catch (e: Exception) {
            0
        }
    }
}
