package com.michalkulik.samsungtvremote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.michalkulik.samsungtvremote.data.RemoteCommand
import com.michalkulik.samsungtvremote.data.SettingsStore
import com.michalkulik.samsungtvremote.data.TvApiClient
import com.michalkulik.samsungtvremote.data.TvStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan ekranu pilota. */
data class RemoteUiState(
    val status: TvStatus = TvStatus(),
    val checking: Boolean = true,
    val busy: String? = null, // id wciśniętego przycisku (spinner)
    val message: String? = null, // snackbar
    val baseUrl: String = SettingsStore.DEFAULT_BASE_URL,
    val apiToken: String = "",
)

class RemoteViewModel(app: Application) : AndroidViewModel(app) {

    private val api = TvApiClient(app)
    private val settings = SettingsStore(app)

    private val _ui = MutableStateFlow(RemoteUiState())
    val ui: StateFlow<RemoteUiState> = _ui.asStateFlow()

    private var pollJob: Job? = null

    // Debounce przyciskow: TV potrafi zdublowac klawisz, gdy dwa requesty
    // przyjda pod rzad (np. podwojne tapniecie, drgniecie palca). Ten sam
    // przycisk ignorujemy przez 600 ms od poprzedniego tapniecia.
    private val lastTapAt = mutableMapOf<String, Long>()
    private val tapGuardMs = 600L

    /** Tapniecie przepuszczone przez debounce; false = zignorowane jako dublet. */
    private fun tapAllowed(busyId: String): Boolean {
        val now = android.os.SystemClock.uptimeMillis()
        val last = lastTapAt[busyId] ?: 0L
        if (now - last < tapGuardMs) return false
        lastTapAt[busyId] = now
        return true
    }

    init {
        viewModelScope.launch {
            settings.baseUrlFlow().collect { url ->
                _ui.value = _ui.value.copy(baseUrl = url)
                refresh()
            }
        }
        viewModelScope.launch {
            settings.apiTokenFlow().collect { token ->
                _ui.value = _ui.value.copy(apiToken = token)
            }
        }
        startPolling()
    }

    fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                refresh(silent = true)
                delay(15_000)
            }
        }
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _ui.value = _ui.value.copy(checking = true)
            val status = api.status()
            _ui.value = _ui.value.copy(status = status, checking = false)
        }
    }

    /** Wysyła komendę; `busyId` pokazuje spinner na przycisku do końca wywołania. */
    fun send(cmd: RemoteCommand, busyId: String, refreshAfter: Boolean = true) {
        if (!tapAllowed(busyId)) return
        if (_ui.value.busy != null) return // poprzednia komenda jeszcze leci — nie kolejkuj
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = busyId, message = null)
            val result = api.send(cmd)
            result
                .onSuccess { detail ->
                    if (!detail.isNullOrBlank()) _ui.value = _ui.value.copy(message = detail)
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(message = e.message ?: "Błąd połączenia")
                }
            _ui.value = _ui.value.copy(busy = null)
            if (refreshAfter) refresh(silent = true)
        }
    }

    fun sendChannel(number: String) {
        if (number.isBlank() || number.any { !it.isDigit() }) {
            _ui.value = _ui.value.copy(message = "Numer kanału musi być liczbą")
            return
        }
        send(RemoteCommand.setChannel(number), busyId = "channel_go")
    }

    fun runApp(appId: String) {
        if (appId.isBlank()) {
            _ui.value = _ui.value.copy(message = "Podaj App ID")
            return
        }
        send(RemoteCommand.runApp(appId.trim()), busyId = "app_go")
    }

    fun saveSettings(baseUrl: String, apiToken: String) {
        settings.save(baseUrl, apiToken)
        _ui.value = _ui.value.copy(message = "Zapisano ustawienia")
        refresh()
    }

    fun consumeMessage() {
        _ui.value = _ui.value.copy(message = null)
    }
}
