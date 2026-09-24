package com.michalkulik.samsungtvremote.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.michalkulik.samsungtvremote.data.RemoteCommand
import com.michalkulik.samsungtvremote.data.SettingsStore
import com.michalkulik.samsungtvremote.data.TvApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Pojedynczy receiver dla dotknięć przycisków we wszystkich trzech widgetach.
 * Akcja niesie ścieżkę API (`extra_path`) i opcjonalne body (`extra_body`).
 * Po wykonaniu odświeża wszystkie widgety (np. kropkę statusu).
 */
class WidgetActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SEND) return
        val path = intent.getStringExtra(EXTRA_PATH) ?: return
        val body = intent.getStringExtra(EXTRA_BODY)
        val pending = goAsync()
        scope.launch {
            try {
                TvApiClient(context).send(RemoteCommand("POST", path, body))
            } finally {
                runCatching {
                    val manager = GlanceAppWidgetManager(context)
                    manager.getGlanceIds(RemoteWidgetFull::class.java).forEach {
                        RemoteWidgetFull().update(context, it)
                    }
                    manager.getGlanceIds(RemoteWidgetPower::class.java).forEach {
                        RemoteWidgetPower().update(context, it)
                    }
                    manager.getGlanceIds(RemoteWidgetCompact::class.java).forEach {
                        RemoteWidgetCompact().update(context, it)
                    }
                }
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_SEND = "com.michalkulik.samsungtvremote.widget.SEND"
        const val EXTRA_PATH = "extra_path"
        const val EXTRA_BODY = "extra_body"

        fun intentFor(context: Context, cmd: RemoteCommand): Intent =
            Intent(context, WidgetActionReceiver::class.java).apply {
                action = ACTION_SEND
                putExtra(EXTRA_PATH, cmd.path)
                cmd.body?.let { putExtra(EXTRA_BODY, it) }
                // unikalność intentu = ścieżka + body, inaczej system zleje przyciski
                data = android.net.Uri.parse("tvremote://${cmd.path}?b=${cmd.body.hashCode()}")
            }

        /** Szybki odczyt statusu do kropki w widgecie (timeout ~6 s, bez tokenu w logach). */
        suspend fun quickStatus(context: Context): Boolean {
            return try {
                TvApiClient(context).status().reachable
            } catch (_: Exception) {
                false
            }
        }

        fun baseUrlLabel(context: Context): String = SettingsStore.baseUrl(context)
    }
}
