package com.michalkulik.samsungtvremote.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.michalkulik.samsungtvremote.data.RemoteCommand

/**
 * Widget 1/3 — pełny pilot: D-pad + OK, cofnij, home, vol+/vol-, P+/P-,
 * power, source. Bez pól tekstowych (Glance nie ma inputu).
 */
class RemoteWidgetFull : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        refreshOnlineFlag(context, id)
        provideContent {
            val prefs = currentState<Preferences>()
            val online = prefs[WidgetKeys.ONLINE] ?: false
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.background())
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WidgetUi.StatusRow(online, WidgetActionReceiver.baseUrlLabel(context))
                Spacer(GlanceModifier.height(4.dp))
                WidgetRow(context, listOf("UP" to RemoteCommand.nav("up")))
                WidgetRow(
                    context,
                    listOf(
                        "LEFT" to RemoteCommand.nav("left"),
                        "OK" to RemoteCommand.nav("enter"),
                        "RIGHT" to RemoteCommand.nav("right"),
                    ),
                )
                WidgetRow(context, listOf("DOWN" to RemoteCommand.nav("down")))
                WidgetRow(
                    context,
                    listOf(
                        "BACK" to RemoteCommand.nav("back"),
                        "HOME" to RemoteCommand.nav("home"),
                    ),
                )
                WidgetRow(
                    context,
                    listOf(
                        "V-" to RemoteCommand.volumeDown(),
                        "MUTE" to RemoteCommand.mute(),
                        "V+" to RemoteCommand.volumeUp(),
                    ),
                )
                WidgetRow(
                    context,
                    listOf(
                        "P-" to RemoteCommand.channelDown(),
                        "P+" to RemoteCommand.channelUp(),
                    ),
                )
                WidgetRow(
                    context,
                    listOf(
                        "POWER" to RemoteCommand.powerToggle(),
                        "SRC" to RemoteCommand.nav("source"),
                    ),
                )
                WidgetUi.OpenAppRow(context)
            }
        }
    }

    @Composable
    private fun WidgetRow(context: Context, buttons: List<Pair<String, RemoteCommand>>) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            buttons.forEach { (label, cmd) ->
                with(WidgetUi) { RowScopeButton(label, cmd, context) }
            }
        }
    }
}

class RemoteWidgetFullReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RemoteWidgetFull()
}
