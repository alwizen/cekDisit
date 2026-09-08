package com.solu8i.compcheck.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scanner_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DRIVER_ID = "driver_id"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_DRIVER_ROLE = "driver_role"
        private const val KEY_DRIVER_NO = "driver_no"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_DARK_MODE = "dark_mode"
        const val DEFAULT_BASE_URL = "http://192.168.110.112:8000/"
    }

    fun saveSession(driverId: Int, driverNo: String, name: String, role: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_DRIVER_ID, driverId)
            putString(KEY_DRIVER_NO, driverNo)
            putString(KEY_DRIVER_NAME, name)
            putString(KEY_DRIVER_ROLE, role)
            apply()
        }
    }

    fun saveBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun saveDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getDriverId(): Int {
        return prefs.getInt(KEY_DRIVER_ID, -1)
    }

    fun getDriverName(): String? {
        return prefs.getString(KEY_DRIVER_NAME, "")
    }

    fun getDriverRole(): String? {
        return prefs.getString(KEY_DRIVER_ROLE, null)
    }

    fun logout() {
        val savedUrl = getBaseUrl()
        prefs.edit().clear().apply()
        saveBaseUrl(savedUrl) // preserve base url after logout
    }
}