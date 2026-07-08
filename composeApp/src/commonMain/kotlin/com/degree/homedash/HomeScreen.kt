package com.degree.homedash

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardScaffold

/** App launcher: a title plus one tappable card per dashboard. */
@Composable
fun HomeScreen(
    onOpenOffice: () -> Unit,
    onOpenPlants: () -> Unit,
    onOpenLivingRoom: () -> Unit,
    onOpenSettings: () -> Unit,
    showOffice: Boolean = false,
) {
    DashboardScaffold(title = "Home", onOpenSettings = onOpenSettings) {
        // Office dashboard is gated behind the viewOfficeScreen feature flag.
        if (showOffice) DashboardCard("Office", Icons.Filled.Chair, AppColors.Wet, onOpenOffice)
        DashboardCard("Plants", Icons.Filled.LocalFlorist, AppColors.Healthy, onOpenPlants)
        DashboardCard("Living Room", Icons.Filled.Weekend, AppColors.Accent, onOpenLivingRoom)
    }
}

@Composable
private fun DashboardCard(
    title: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(20.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(onOpenOffice = {}, onOpenPlants = {}, onOpenLivingRoom = {}, onOpenSettings = {}, showOffice = true)
        }
    }
}
