package com.degree.homedash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.network.ConnectionStatus

/**
 * App-wide Home Assistant connection status, provided once at the app root so every
 * [DashboardHeader] can show its status dot without each screen threading it through.
 */
val LocalConnectionStatus = compositionLocalOf<ConnectionStatus> { ConnectionStatus.Disconnected }

/**
 * Standard dashboard page layout: a flush, full-bleed [DashboardHeader] above a vertically
 * scrolling, 16dp-padded [content] column (sections spaced by [Dimens.SectionSpacing]). Keeps the
 * header out of the content padding so its Columbia-blue bar spans edge to edge.
 */
@Composable
fun DashboardScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    connection: ConnectionStatus = LocalConnectionStatus.current,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DashboardHeader(title, onBack, onOpenSettings, connection)
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            content = content,
        )
    }
}

/**
 * Shared dashboard header: optional back arrow, title, a [connection] status dot, and an optional
 * Settings (gear) action. A full-width Columbia-blue bar whose background extends up under the status
 * bar (content is inset below it via [WindowInsets.statusBars]), so place it flush at the top of the
 * screen, not inside content padding. [connection] defaults to [LocalConnectionStatus].
 */
@Composable
fun DashboardHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    connection: ConnectionStatus = LocalConnectionStatus.current,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.ColumbiaBlue,
        contentColor = AppColors.ColumbiaBlueOn,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(start = if (onBack != null) 0.dp else 16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(connectionColor(connection)),
            )
            if (onOpenSettings != null) {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
        }
    }
}

/** Connection status → indicator color: green connected, amber connecting, red error, gray offline. */
private fun connectionColor(status: ConnectionStatus): Color = when (status) {
    ConnectionStatus.Connected -> AppColors.StatusGreen
    ConnectionStatus.Connecting -> AppColors.StatusAmber
    is ConnectionStatus.Error -> AppColors.StatusRed
    ConnectionStatus.Disconnected -> AppColors.StatusGray
}

@Preview
@Composable
private fun DashboardHeaderPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Root screen: gear only, connected.
            DashboardHeader("Office", onOpenSettings = {}, connection = ConnectionStatus.Connected)
            // Nested screen: back + gear, connecting.
            DashboardHeader("Plants", onBack = {}, onOpenSettings = {}, connection = ConnectionStatus.Connecting)
            // Graph screen: back only, error.
            DashboardHeader("Water Level", onBack = {}, connection = ConnectionStatus.Error("timeout"))
            // Disconnected (gray), gear only.
            DashboardHeader("Living Room", onOpenSettings = {}, connection = ConnectionStatus.Disconnected)
        }
    }
}
