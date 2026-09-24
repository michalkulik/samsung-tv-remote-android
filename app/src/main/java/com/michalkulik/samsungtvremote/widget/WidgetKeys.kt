package com.michalkulik.samsungtvremote.widget

import androidx.datastore.preferences.core.booleanPreferencesKey

/** Klucze stanu widgetów Glance. */
internal object WidgetKeys {
    val ONLINE = booleanPreferencesKey("online")
}
