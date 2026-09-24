package com.michalkulik.samsungtvremote.widget

import android.content.Context
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

/** Widget 3/3 — power + source + OK. */
class RemoteWidgetCompact : GlanceAppWidget() {

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
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    with(WidgetUi) { RowScopeButton("POWER", RemoteCommand.powerToggle(), context) }
                    with(WidgetUi) { RowScopeButton("SRC", RemoteCommand.nav("source"), context) }
                    with(WidgetUi) { RowScopeButton("OK", RemoteCommand.nav("enter"), context) }
                }
                WidgetUi.OpenAppRow(context)
            }
        }
    }
}

class RemoteWidgetCompactReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RemoteWidgetCompact()
}
