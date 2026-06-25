package com.degree.homedash.office

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ClimateRowPreview() = ControlPreview {
    ClimateRow("Temperature", previewEntity("72.5"), Icons.Filled.Thermostat, Color(0xFFFF8A65))
    ClimateRow("Humidity", previewEntity("48"), Icons.Filled.WaterDrop, Color(0xFF4FC3F7))
    ClimateRow("Unavailable", previewEntity("unavailable"), Icons.Filled.Thermostat, Color(0xFFFF8A65))
}
