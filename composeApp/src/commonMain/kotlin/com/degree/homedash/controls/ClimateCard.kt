package com.degree.homedash.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * Read-only sensor card: tinted [icon] pinned top-left, with the [valueText] (prominent) above the
 * [label], centered. Non-interactive — climate readings have no toggle. Matches [EntityCard]'s tile
 * shape/height so it lines up with the light/fan cards in a shared grid.
 */
@Composable
internal fun ClimateCard(
    label: String,
    valueText: String,
    subvalueText: String? = null,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.height(Dimens.EntityCardHeight),
    ) {
        Box(Modifier.fillMaxSize().padding(Dimens.EntityCardPadding)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.align(Alignment.TopStart).size(Dimens.RowIconSize),
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                if (subvalueText != null) {
                    Text(
                        text = subvalueText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ClimateCardPreview() = ControlPreview {
    ClimateCard(
        label = "Temperature",
        valueText = "72.5 °F",
        subvalueText = null,
        icon = Icons.Filled.Thermostat,
        tint = AppColors.TempWarm,
        modifier = Modifier.size(180.dp, height = Dimens.EntityCardHeight)
    )
    ClimateCard(
        label = "Humidity",
        valueText = "48.5 °F",
        subvalueText = "39%",
        icon = Icons.Filled.WaterDrop,
        tint = AppColors.Wet,
        modifier = Modifier.size(180.dp, Dimens.EntityCardHeight)
    )
}
