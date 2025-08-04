package iss.nus.edu.sg.appfiles.feature_login.util


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorageManager(context: Context) {

    companion object {
        private const val PREF_NAME = "secure_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
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
        sharedPrefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPrefs.getString(KEY_TOKEN, null)
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
}
