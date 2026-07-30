package com.degree.homedash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.degree.homedash.controls.ControlLayout
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.EntityControl
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.previewNavigation
import com.degree.homedash.shared.model.entity.NavigationMetadata.NavigationTarget
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardScaffold

@Composable
fun HomeScreen(
    onNavigate: (NavigationTarget) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val vm: HomeViewModel = koinViewModel()
    val warnings by vm.warnings.collectAsStateWithLifecycle()
    HomeContent(
        warnings = warnings,
        navigation = vm.navigation,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings,
    )
}

/** App launcher: any active warnings, then one tappable card per dashboard. */
@Composable
fun HomeContent(
    warnings: List<HomeWarning>,
    navigation: List<EntityUi.Navigation>,
    onNavigate: (NavigationTarget) -> Unit,
    onOpenSettings: () -> Unit,
) {
    DashboardScaffold(
        title = "Home",
        onOpenSettings = onOpenSettings,
        versionLabel = "v${AppInfo.VERSION}",
    ) {
        // Warnings stay full-width above the grid.
        warnings.forEach { WarningCard(it) }

        val onAction: (EntityAction) -> Unit = { action ->
            if (action is EntityAction.Navigate) onNavigate(action.target)
        }
        // A 2-column tile grid, laid out here rather than via ControlGroup so the launcher keeps its
        // bare look — no group wrapper, no section title. Its own 8dp spacing makes the grid read as
        // one block instead of inheriting the scaffold's wider section gaps.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            navigation.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { entity ->
                        Box(Modifier.weight(1f)) {
                            EntityControl(entity, ControlLayout.Card, onAction, Modifier.fillMaxWidth())
                        }
                    }
                    // Pad an odd final row so its tile keeps the grid's column width.
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
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

/** The launcher roster as the repo declares it, for previews. */
private val previewNavigationCards: List<EntityUi.Navigation> = listOf(
    previewNavigation(NavigationTarget.Office, "Office"),
    previewNavigation(NavigationTarget.Plants, "Plants"),
    previewNavigation(NavigationTarget.LivingRoom, "Living Room"),
    previewNavigation(NavigationTarget.Pets, "Pets"),
)

@Preview(widthDp = 380, heightDp = 560)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeContent(
                warnings = listOf(HomeWarning("Cat water running low — 24 %", WarningSeverity.Warning)),
                navigation = previewNavigationCards,
                onNavigate = {},
                onOpenSettings = {},
            )
        }
    }
}
