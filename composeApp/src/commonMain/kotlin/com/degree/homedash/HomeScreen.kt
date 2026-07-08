package com.degree.homedash

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardScaffold

@Composable
fun HomeScreen(
    repository: HomeAssistantRepo,
    onOpenOffice: () -> Unit,
    onOpenPlants: () -> Unit,
    onOpenLivingRoom: () -> Unit,
    onOpenSettings: () -> Unit,
    showOffice: Boolean = false,
) {
    val vm: HomeViewModel = viewModel { HomeViewModel(repository) }
    val warnings by vm.warnings.collectAsStateWithLifecycle()
    HomeContent(
        warnings = warnings,
        onOpenOffice = onOpenOffice,
        onOpenPlants = onOpenPlants,
        onOpenLivingRoom = onOpenLivingRoom,
        onOpenSettings = onOpenSettings,
        showOffice = showOffice,
    )
}

/** App launcher: any active warnings, then one tappable card per dashboard. */
@Composable
fun HomeContent(
    warnings: List<HomeWarning>,
    onOpenOffice: () -> Unit,
    onOpenPlants: () -> Unit,
    onOpenLivingRoom: () -> Unit,
    onOpenSettings: () -> Unit,
    showOffice: Boolean = false,
) {
    DashboardScaffold(title = "Home", onOpenSettings = onOpenSettings) {
        warnings.forEach { WarningCard(it) }
        // Office dashboard is gated behind the viewOfficeScreen feature flag.
        if (showOffice) DashboardCard("Office", Icons.Filled.Chair, AppColors.Wet, onOpenOffice)
        DashboardCard("Plants", Icons.Filled.LocalFlorist, AppColors.Healthy, onOpenPlants)
        DashboardCard("Living Room", Icons.Filled.Weekend, AppColors.Accent, onOpenLivingRoom)
    }
}

@Composable
private fun WarningCard(warning: HomeWarning) {
    val color = when (warning.severity) {
        WarningSeverity.Warning -> AppColors.StatusAmber
        WarningSeverity.Critical -> AppColors.StatusRed
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.Black),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
            Text(
                text = warning.message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
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

@Preview(widthDp = 380, heightDp = 460)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeContent(
                warnings = listOf(HomeWarning("Cat water running low — 24 %", WarningSeverity.Warning)),
                onOpenOffice = {},
                onOpenPlants = {},
                onOpenLivingRoom = {},
                onOpenSettings = {},
                showOffice = true,
            )
        }
    }
}
