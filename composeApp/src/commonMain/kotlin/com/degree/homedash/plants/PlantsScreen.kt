package com.degree.homedash.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.SectionCard

/** Soil-moisture sensor entity ids shown on the Plants dashboard, in display order. */
object PlantEntities {
    val SOIL_MOISTURE = listOf(
        "sensor.louie_moisture_sensor_soil_moisture",
    )
}

@Composable
fun PlantsScreen(repository: HaRepository, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val states by repository.states.collectAsState()
    val plants = PlantEntities.SOIL_MOISTURE.mapNotNull { states[it] }
    PlantsContent(plants = plants, onBack = onBack, onOpenSettings = onOpenSettings)
}

/** Stateless Plants UI — soil-moisture readings in, navigation actions out. */
@Composable
fun PlantsContent(
    plants: List<EntityState>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardHeader("Plants", onBack = onBack, onOpenSettings = onOpenSettings)

        SectionCard("Soil Moisture") {
            if (plants.isEmpty()) {
                Text(
                    "No moisture sensors found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                plants.forEach { SoilMoistureControl(it) }
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 600)
@Composable
private fun PlantsScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlantsContent(plants = previewPlants, onBack = {}, onOpenSettings = {})
        }
    }
}
