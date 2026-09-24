package com.michalkulik.samsungtvremote.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Klient HTTP usługi samsung-tv-remote (FastAPI na hoście 192.168.68.19:9039).
 * Bez Retrofit — jeden OkHttp + kotlinx.serialization, żeby APK był mały.
 */
class TvApiClient(context: Context) {

    private val appContext = context.applicationContext

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS) // /api/power/on czeka na wybudzenie TV
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: Flow<String?> = _lastError.asStateFlow()

    private fun baseUrl(): String {
        val raw = SettingsStore.baseUrl(appContext)
        val withScheme = if (raw.startsWith("http")) raw else "http://$raw"
        return withScheme.trimEnd('/')
    }

    private fun Request.Builder.withAuth(): Request.Builder {
        val token = SettingsStore.apiToken(appContext)
        if (token.isNotBlank()) header("X-Api-Token", token)
        return this
    }

    suspend fun status(): TvStatus = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(baseUrl() + "/api/status").withAuth().get().build()
            http.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext TvStatus.offline("HTTP ${response.code}")
                val root = json.decodeFromString<ApiResult>(body)
                val data = root.data?.jsonObject
                if (root.ok && data != null) {
                    TvStatus(
                        reachable = data["reachable"]?.jsonPrimitive?.content == "true",
                        powerState = data["powerState"]?.jsonPrimitive?.content ?: "unknown",
                        name = data["name"]?.jsonPrimitive?.content ?: "",
                        modelName = data["modelName"]?.jsonPrimitive?.content ?: "",
                        ip = data["ip"]?.jsonPrimitive?.content ?: "",
                    )
                } else {
                    TvStatus.offline(root.data?.toString() ?: root.detail)
                }
            }
        } catch (e: Exception) {
            TvStatus.offline(e.message ?: e.javaClass.simpleName)
        }
    }

    /** Wykonuje komendę pilota. Zwraca komunikat do pokazania w UI (null = OK bez tekstu). */
    suspend fun send(cmd: RemoteCommand): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val body = (cmd.body ?: "{}").toRequestBody("application/json".toMediaType())
            val builder = Request.Builder().url(baseUrl() + cmd.path).withAuth()
            if (cmd.method == "POST") builder.post(body) else builder.get()
            http.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching {
                        json.decodeFromString<ApiResult>(text).detail.ifBlank { "HTTP ${response.code}" }
                    }.getOrElse { "HTTP ${response.code}" }
                    setLastError(detail)
                    return@withContext Result.failure(RuntimeException(detail))
                }
                val detail = runCatching {
                    val root = json.decodeFromString<ApiResult>(text)
                    root.detail.ifBlank {
                        root.data?.jsonObject?.get("detail")?.jsonPrimitive?.content
                    }
                }.getOrNull()
                setLastError(null)
                Result.success(detail)
            }
        } catch (e: Exception) {
            val msg = e.message ?: e.javaClass.simpleName
            setLastError(msg)
            Result.failure(RuntimeException(msg, e))
        }
    }

    private fun setLastError(value: String?) {
        _lastError.value = value
    }
}
