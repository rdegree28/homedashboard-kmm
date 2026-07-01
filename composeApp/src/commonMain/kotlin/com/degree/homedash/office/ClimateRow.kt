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
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.formatNumberOrSelf

/** A read-only sensor readout: icon + label + formatted value (e.g. "76.3 °F"). */
@Composable
fun ClimateRow(label: String, entity: EntityState?, icon: ImageVector, tint: Color) {
    val unit = entity?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        entity == null || entity.isUnavailable -> "—"
        else -> {
            val num = formatNumberOrSelf(entity.state, decimals = 1)
            if (unit.isNotEmpty()) "$num $unit" else num
        }
    }
    EntityRow(
        label = label,
        leading = {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(Dimens.RowIconSize))
        },
        trailing = {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ClimateRowPreview() = ControlPreview {
    ClimateRow("Temperature", previewEntity("72.5"), Icons.Filled.Thermostat, AppColors.TempWarm)
    ClimateRow("Humidity", previewEntity("48"), Icons.Filled.WaterDrop, AppColors.Wet)
    ClimateRow("Unavailable", previewEntity("unavailable"), Icons.Filled.Thermostat, AppColors.TempWarm)
}
