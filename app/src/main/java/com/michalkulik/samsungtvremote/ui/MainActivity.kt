package com.michalkulik.samsungtvremote.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.michalkulik.samsungtvremote.ui.theme.SamsungTvRemoteTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SamsungTvRemoteTheme {
                RemoteApp()
            }
        }
    }
}
