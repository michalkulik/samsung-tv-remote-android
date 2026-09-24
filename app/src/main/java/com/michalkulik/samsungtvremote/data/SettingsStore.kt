package com.michalkulik.samsungtvremote.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Ustawienia połączenia z usługą samsung-tv-remote (SharedPreferences, bez migracji). */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun baseUrlFlow(): Flow<String> = stringFlow(KEY_BASE_URL, DEFAULT_BASE_URL)
    fun apiTokenFlow(): Flow<String> = stringFlow(KEY_API_TOKEN, "")

    fun getBaseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    fun getApiToken(): String = prefs.getString(KEY_API_TOKEN, "") ?: ""

    fun save(baseUrl: String, apiToken: String) {
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl.trim().ifBlank { DEFAULT_BASE_URL })
            .putString(KEY_API_TOKEN, apiToken.trim())
            .apply()
    }

    private fun stringFlow(key: String, default: String): Flow<String> = callbackFlow {
        trySend(prefs.getString(key, default) ?: default)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
            if (changed == null || changed == key) trySend(prefs.getString(key, default) ?: default)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    companion object {
        private const val FILE_NAME = "tv_remote_settings"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_TOKEN = "api_token"

        const val DEFAULT_BASE_URL = "192.168.68.19:9039"

        /** Odczyt synchroniczny dla widgetów (bez kontekstu DI). */
        fun baseUrl(context: Context): String =
            context.applicationContext
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

        fun apiToken(context: Context): String =
            context.applicationContext
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_TOKEN, "") ?: ""

        fun baseUrlOrDefault(value: String?): String =
            if (value.isNullOrBlank()) DEFAULT_BASE_URL else value
    }
}

/** Mapowanie Flow<String> na Boolean dla wygody w UI. */
fun Flow<String>.isNotBlankFlow(): Flow<Boolean> = map { it.isNotBlank() }.distinctUntilChanged()
