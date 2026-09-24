package com.michalkulik.samsungtvremote.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.michalkulik.samsungtvremote.R
import com.michalkulik.samsungtvremote.data.RemoteCommand
import com.michalkulik.samsungtvremote.ui.MainActivity

/** Wspólne klocki Glance: przyciski, kropka statusu, odświeżanie stanu. */
internal object WidgetUi {

    @Composable
    fun RemoteButton(
        label: String,
        cmd: RemoteCommand,
        context: Context,
        modifier: GlanceModifier = GlanceModifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            modifier = modifier
                .padding(4.dp)
                .background(GlanceTheme.button())
                .clickable(actionSendBroadcast(WidgetActionReceiver.intentFor(context, cmd)))
                .padding(10.dp),
            style = TextStyle(color = GlanceTheme.onButton()),
            maxLines = 1,
        )
    }

    @Composable
    fun RowScope.RowScopeButton(label: String, cmd: RemoteCommand, context: Context) {
        Text(
            text = label,
            modifier = GlanceModifier
                .defaultWeight()
                .padding(4.dp)
                .background(GlanceTheme.button())
                .clickable(actionSendBroadcast(WidgetActionReceiver.intentFor(context, cmd)))
                .padding(10.dp),
            style = TextStyle(color = GlanceTheme.onButton()),
            maxLines = 1,
        )
    }

    @Composable
    fun StatusRow(online: Boolean, label: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (online) "●" else "○",
                style = TextStyle(color = if (online) GlanceTheme.online() else GlanceTheme.offline()),
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(text = label, style = TextStyle(color = GlanceTheme.text()), maxLines = 1)
        }
    }

    @Composable
    fun OpenAppRow(context: Context) {
        Text(
            text = "⧉ Otwórz pilota",
            modifier = GlanceModifier
                .padding(top = 4.dp)
                .clickable(actionStartActivity(MainActivity::class.java))
                .padding(6.dp),
            style = TextStyle(color = GlanceTheme.text()),
        )
    }
}

internal object GlanceTheme {
    fun button() = ColorProvider(R.color.widget_button)
    fun onButton() = ColorProvider(R.color.widget_on_button)
    fun text() = ColorProvider(R.color.widget_text)
    fun online() = ColorProvider(R.color.widget_online)
    fun offline() = ColorProvider(R.color.widget_offline)
    fun background() = ColorProvider(R.color.widget_background)
}

internal suspend fun refreshOnlineFlag(context: Context, glanceId: GlanceId) {
    val online = WidgetActionReceiver.quickStatus(context)
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        prefs.toMutablePreferences().apply { this[WidgetKeys.ONLINE] = online }
    }
}
