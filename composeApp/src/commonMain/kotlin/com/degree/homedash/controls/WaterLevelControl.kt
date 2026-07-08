package com.degree.homedash.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors

/**
 * A percentage-level readout: name, a colored fill bar, and the percentage. When [onClick] is
 * provided the whole row is tappable (and shows a chevron) — used to open the history graph.
 */
@Composable
fun WaterLevelControl(
    ui: EntityUi.WaterLevel,
    onClick: (() -> Unit)? = null,
) {
    val fraction = ui.pct?.let { (it / 100.0).coerceIn(0.0, 1.0) } ?: 0.0
    val barColor = waterLevelColor(ui.pct)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ui.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = ui.valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = barColor,
            )
            if (onClick != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.toFloat())
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor),
            )
        }
    }
}

/** Water-level status color: red (<10, refill) → yellow (10–35) → blue (>35, full). */
internal fun waterLevelColor(pct: Double?): Color = when {
    pct == null -> AppColors.StatusGray
    pct < 10 -> AppColors.StatusRed // needs refill
    pct < 35 -> AppColors.StatusAmber // getting low
    else -> AppColors.Wet // full
}
