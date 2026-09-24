package com.michalkulik.samsungtvremote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.michalkulik.samsungtvremote.R
import com.michalkulik.samsungtvremote.data.RemoteCommand

/**
 * Ekran pilota: D-pad z OK, power, source, głośność, kanały (P+/P-, numer),
 * media, nawigacja systemowa, App ID oraz ustawienia połączenia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteApp(vm: RemoteViewModel = viewModel()) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                actions = {
                    StatusDot(
                        online = state.status.reachable,
                        checking = state.checking,
                    )
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Odśwież")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(
                online = state.status.reachable,
                checking = state.checking,
                model = state.status.modelName,
                ip = state.status.ip,
                detail = state.status.detail,
            )

            SectionCard(stringResource(R.string.section_power)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RemoteButton(
                        id = "power_toggle",
                        label = stringResource(R.string.action_power),
                        icon = Icons.Filled.PowerSettingsNew,
                        busy = state.busy,
                        primary = true,
                        onClick = { vm.send(RemoteCommand.powerToggle(), "power_toggle") },
                    )
                    RemoteButton(
                        id = "power_on",
                        label = stringResource(R.string.action_power_on),
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.powerOn(), "power_on") },
                    )
                    RemoteButton(
                        id = "power_off",
                        label = stringResource(R.string.action_power_off),
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.powerOff(), "power_off") },
                    )
                }
            }

            SectionCard(stringResource(R.string.section_nav)) {
                DPad(
                    busy = state.busy,
                    onNav = { action, id -> vm.send(RemoteCommand.nav(action), id) },
                )
                Spacer(Modifier.height(8.dp))
                ButtonGrid(
                    busy = state.busy,
                    buttons = listOf(
                        GridAction("back", stringResource(R.string.action_back), Icons.AutoMirrored.Filled.ArrowBack, RemoteCommand.nav("back")),
                        GridAction("exit", stringResource(R.string.action_exit), Icons.Filled.Close, RemoteCommand.nav("exit")),
                        GridAction("home", stringResource(R.string.action_home), Icons.Filled.Home, RemoteCommand.nav("home")),
                        GridAction("source", stringResource(R.string.action_source), Icons.Filled.Input, RemoteCommand.nav("source")),
                        GridAction("menu", stringResource(R.string.action_menu), Icons.Filled.Menu, RemoteCommand.nav("menu")),
                        GridAction("guide", stringResource(R.string.action_guide), null, RemoteCommand.nav("guide")),
                        GridAction("tools", stringResource(R.string.action_tools), Icons.Filled.Settings, RemoteCommand.nav("tools")),
                        GridAction("info", stringResource(R.string.action_info), null, RemoteCommand.nav("info")),
                    ),
                    onClick = { action, id -> vm.send(action, id) },
                )
            }

            SectionCard(stringResource(R.string.section_volume)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RemoteButton(
                        id = "vol_down",
                        label = "−",
                        icon = Icons.AutoMirrored.Filled.VolumeDown,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.volumeDown(), "vol_down") },
                    )
                    RemoteButton(
                        id = "mute",
                        label = stringResource(R.string.action_mute),
                        icon = Icons.Filled.VolumeOff,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.mute(), "mute") },
                    )
                    RemoteButton(
                        id = "vol_up",
                        label = "+",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.volumeUp(), "vol_up") },
                    )
                }
            }

            SectionCard(stringResource(R.string.section_channels)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RemoteButton(
                        id = "ch_down",
                        label = "P−",
                        icon = Icons.Filled.Remove,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.channelDown(), "ch_down") },
                    )
                    RemoteButton(
                        id = "ch_list",
                        label = stringResource(R.string.action_channel_list),
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.channelList(), "ch_list") },
                    )
                    RemoteButton(
                        id = "ch_up",
                        label = "P+",
                        icon = Icons.Filled.Add,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.channelUp(), "ch_up") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                var channel by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = channel,
                        onValueChange = { channel = it.filter(Char::isDigit).take(4) },
                        label = { Text(stringResource(R.string.action_channel_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { vm.sendChannel(channel) }) {
                        Text(stringResource(R.string.action_go_channel))
                    }
                }
            }

            SectionCard(stringResource(R.string.section_media)) {
                ButtonGrid(
                    busy = state.busy,
                    buttons = listOf(
                        GridAction("play", "▶", null, RemoteCommand.media("play")),
                        GridAction("pause", "⏸", null, RemoteCommand.media("pause")),
                        GridAction("stop", "⏹", null, RemoteCommand.media("stop")),
                        GridAction("rewind", "⏪", null, RemoteCommand.media("rewind")),
                        GridAction("forward", "⏩", null, RemoteCommand.media("forward")),
                        GridAction("live", "LIVE", null, RemoteCommand.media("live")),
                    ),
                    onClick = { action, id -> vm.send(action, id) },
                )
            }

            SectionCard(stringResource(R.string.section_apps)) {
                var appId by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = appId,
                        onValueChange = { appId = it.filter(Char::isDigit).take(16) },
                        label = { Text(stringResource(R.string.action_app_id_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { vm.runApp(appId) }) {
                        Text(stringResource(R.string.action_run_app))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    RemoteButton(
                        id = "pair",
                        label = stringResource(R.string.action_pair),
                        icon = Icons.Filled.Link,
                        busy = state.busy,
                        onClick = { vm.send(RemoteCommand.pair(), "pair", refreshAfter = true) },
                    )
                }
            }

            SectionCard(stringResource(R.string.section_settings)) {
                var host by remember(state.baseUrl) { mutableStateOf(state.baseUrl) }
                var token by remember(state.apiToken) { mutableStateOf(state.apiToken) }
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.settings_host)) },
                    placeholder = { Text(stringResource(R.string.settings_host_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(stringResource(R.string.settings_token)) },
                    placeholder = { Text(stringResource(R.string.settings_token_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.saveSettings(host, token) }) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

private data class GridAction(
    val id: String,
    val label: String,
    val icon: ImageVector?,
    val cmd: RemoteCommand,
)

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun StatusCard(online: Boolean, checking: Boolean, model: String, ip: String, detail: String) {
    val text = when {
        checking -> stringResource(R.string.status_checking)
        online -> listOf(stringResource(R.string.status_online), model, ip).filter { it.isNotBlank() }.joinToString(" · ")
        else -> if (detail.isNotBlank()) "${stringResource(R.string.status_offline)}: $detail" else stringResource(R.string.status_offline)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(online = online, checking = checking)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatusDot(online: Boolean, checking: Boolean) {
    if (checking) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
    } else {
        val color = if (online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
            drawCircle(color)
        }
    }
}

@Composable
private fun DPad(busy: String?, onNav: (String, String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        DPadButton("up", Icons.Filled.ExpandLess, "Góra", busy) { onNav("up", "up") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DPadButton("left", Icons.Filled.ChevronLeft, "Lewo", busy) { onNav("left", "left") }
            FilledTonalButton(
                onClick = { onNav("enter", "enter") },
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                if (busy == "enter") {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.action_ok), style = MaterialTheme.typography.titleMedium)
                }
            }
            DPadButton("right", Icons.Filled.ChevronRight, "Prawo", busy) { onNav("right", "right") }
        }
        DPadButton("down", Icons.Filled.ExpandMore, "Dół", busy) { onNav("down", "down") }
    }
}

@Composable
private fun DPadButton(id: String, icon: ImageVector, desc: String, busy: String?, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        if (busy == id) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Icon(icon, contentDescription = desc, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun ButtonGrid(
    busy: String?,
    buttons: List<GridAction>,
    onClick: (RemoteCommand, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { action ->
                    RemoteButton(
                        id = action.id,
                        label = action.label,
                        icon = action.icon,
                        busy = busy,
                        modifier = Modifier.weight(1f),
                        onClick = { onClick(action.cmd, action.id) },
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RemoteButton(
    id: String,
    label: String,
    busy: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = if (primary) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    ) {
        if (busy == id) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, maxLines = 1)
        }
    }
}
