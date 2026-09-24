package com.michalkulik.samsungtvremote.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Minimalny model odpowiedzi API samsung-tv-remote (FastAPI). */
@Serializable
data class ApiResult(
    val ok: Boolean = false,
    val detail: String = "",
    val data: JsonElement? = null,
)

@Serializable
data class TvStatus(
    val reachable: Boolean = false,
    val powerState: String = "unknown",
    val name: String = "",
    val modelName: String = "",
    val ip: String = "",
    val detail: String = "",
) {
    companion object {
        fun offline(detail: String = "") = TvStatus(reachable = false, detail = detail)
    }
}

/** Komenda pilota: metoda HTTP + ścieżka + opcjonalne body JSON. */
data class RemoteCommand(
    val method: String,
    val path: String,
    val body: String? = null,
) {
    companion object {
        fun post(path: String, body: String? = null) = RemoteCommand("POST", path, body)

        // Zasilanie
        fun powerToggle() = post("/api/power/toggle")
        fun powerOn() = post("/api/power/on", "{}")
        fun powerOff() = post("/api/power/off")

        // Nawigacja (D-pad + systemowe)
        fun nav(action: String) = post("/api/nav/$action")

        // Dźwięk
        fun volumeUp(steps: Int = 1) = post("/api/volume/up", """{"steps":$steps}""")
        fun volumeDown(steps: Int = 1) = post("/api/volume/down", """{"steps":$steps}""")
        fun mute() = post("/api/mute")

        // Kanały
        fun channelUp() = post("/api/channel/up")
        fun channelDown() = post("/api/channel/down")
        fun channelList() = post("/api/channel/list")
        fun setChannel(number: String) = post("/api/channel", """{"channel":"$number"}""")

        // Media
        fun media(action: String) = post("/api/media/$action")

        // Dowolny klawisz KEY_*
        fun key(key: String, times: Int = 1) = post("/api/key", """{"key":"$key","times":$times}""")

        // Aplikacje
        fun runApp(appId: String) = post("/api/apps/run", """{"app_id":"$appId"}""")
        fun pair() = post("/api/pair")
    }
}
