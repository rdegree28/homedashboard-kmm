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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.ui.icons.RoomIcons

/**
 * App-wide Home Assistant connection status, provided once at the app root so every
 * [DashboardHeader] can show its status dot without each screen threading it through.
 */
val LocalHaConnectionStatus = compositionLocalOf<HaConnectionStatus> { HaConnectionStatus.Disconnected }

/**
 * Standard dashboard page layout: a flush, full-bleed [DashboardHeader] pinned to the top of the
 * screen above a vertically scrolling, 16dp-padded [content] column (sections spaced by
 * [Dimens.SectionSpacing]). Only the content scrolls — the header stays put — and the header sits
 * outside the content padding so its Columbia-blue bar spans edge to edge.
 */
@Composable
fun DashboardScaffold(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    connection: HaConnectionStatus = LocalHaConnectionStatus.current,
    versionLabel: String? = null,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DashboardHeader(title, onBack, onOpenSettings, connection, versionLabel, icon)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Dimens.ScrollBottomPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            content = content,
        )
    }
}

/**
 * Shared dashboard header: optional back arrow, title, an optional [versionLabel], a [connection]
 * status dot, and an optional Settings (gear) action. A full-width Columbia-blue bar whose background
 * extends up under the status bar (content is inset below it via [WindowInsets.statusBars]), so place
 * it flush at the top of the screen, not inside content padding. [connection] defaults to
 * [LocalHaConnectionStatus].
 *
 * [versionLabel] is set by the Home launcher only, to make the running build identifiable. [icon] is
 * the room glyph shown ahead of the title on each dashboard (see `RoomIcons`).
 */
@Composable
fun DashboardHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    connection: HaConnectionStatus = LocalHaConnectionStatus.current,
    versionLabel: String? = null,
    icon: ImageVector? = null,
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
            // Whichever comes first — icon or title — carries the leading inset the back arrow would
            // otherwise provide.
            val leadingInset = if (onBack != null) 0.dp else 16.dp
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(start = leadingInset, end = 10.dp).size(26.dp),
                )
            }
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (icon != null) 0.dp else leadingInset),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (versionLabel != null) {
                Text(
                    text = versionLabel,
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
            }
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
private fun connectionColor(status: HaConnectionStatus): Color = when (status) {
    HaConnectionStatus.Connected -> AppColors.StatusGreen
    HaConnectionStatus.Connecting -> AppColors.StatusAmber
    is HaConnectionStatus.Error -> AppColors.StatusRed
    HaConnectionStatus.Disconnected -> AppColors.StatusGray
}

@Preview
@Composable
private fun DashboardHeaderPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Home: gear + version label.
            DashboardHeader("Home", onOpenSettings = {}, connection = HaConnectionStatus.Connected, versionLabel = "v1.0")
            // Everything at once, to check a long title doesn't crowd the version out.
            DashboardHeader(
                "Living Room Climate",
                onBack = {},
                onOpenSettings = {},
                connection = HaConnectionStatus.Connecting,
                versionLabel = "v1.0",
            )
            // The four room headers, each with its hand-drawn glyph.
            DashboardHeader("Office", onBack = {}, onOpenSettings = {}, icon = RoomIcons.Desk)
            DashboardHeader("Plants", onBack = {}, onOpenSettings = {}, icon = RoomIcons.Plant)
            DashboardHeader("Living Room", onBack = {}, onOpenSettings = {}, icon = RoomIcons.Sofa)
            DashboardHeader("Pets", onBack = {}, onOpenSettings = {}, icon = RoomIcons.Paw)
            // Root screen: gear only, connected.
            DashboardHeader("Office", onOpenSettings = {}, connection = HaConnectionStatus.Connected)
            // Nested screen: back + gear, connecting.
            DashboardHeader("Plants", onBack = {}, onOpenSettings = {}, connection = HaConnectionStatus.Connecting)
            // Graph screen: back only, error.
            DashboardHeader("Water Level", onBack = {}, connection = HaConnectionStatus.Error("timeout"))
            // Disconnected (gray), gear only.
            DashboardHeader("Living Room", onOpenSettings = {}, connection = HaConnectionStatus.Disconnected)
        }
    }
}
