package com.degree.homedash.office

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/** A read-only sensor readout: icon + label + preformatted value (e.g. "76.3 °F"). */
@Composable
fun ClimateRow(ui: SensorUi, icon: ImageVector, tint: Color) {
    EntityRow(
        label = ui.label,
        leading = {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(Dimens.RowIconSize))
        },
        trailing = {
            Text(
                text = ui.valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ClimateRowPreview() = ControlPreview {
    ClimateRow(SensorUi("Temperature", "72.5 °F"), Icons.Filled.Thermostat, AppColors.TempWarm)
    ClimateRow(SensorUi("Humidity", "48 %"), Icons.Filled.WaterDrop, AppColors.Wet)
    ClimateRow(SensorUi("Unavailable", "—"), Icons.Filled.Thermostat, AppColors.TempWarm)
}
