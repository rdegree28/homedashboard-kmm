package com.degree.homedash.office

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState
import kotlin.math.round

/** A read-only sensor readout: icon + label + formatted value (e.g. "76.3 °F"). */
@Composable
fun ClimateRow(label: String, entity: EntityState?, icon: ImageVector, tint: Color) {
    val unit = entity?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        entity == null || entity.isUnavailable -> "—"
        else -> {
            val v = entity.state.toDoubleOrNull()
            val num = if (v != null) {
                val r = round(v * 10.0) / 10.0
                if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
            } else {
                entity.state
            }
            if (unit.isNotEmpty()) "$num $unit" else num
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ClimateRowPreview() = ControlPreview {
    ClimateRow("Temperature", previewEntity("72.5"), Icons.Filled.Thermostat, Color(0xFFFF8A65))
    ClimateRow("Humidity", previewEntity("48"), Icons.Filled.WaterDrop, Color(0xFF4FC3F7))
    ClimateRow("Unavailable", previewEntity("unavailable"), Icons.Filled.Thermostat, Color(0xFFFF8A65))
}
