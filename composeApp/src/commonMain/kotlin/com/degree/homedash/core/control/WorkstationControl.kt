package com.degree.homedash.core.control

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.core.device.OfficeWorkstationUi
import com.degree.homedash.core.util.ControlPreview
import com.degree.homedash.core.util.DeviceToggleRow
import com.degree.homedash.core.util.PowerGraph
import com.degree.homedash.core.util.previewHistory
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.device_metadata.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.device_state.OfficeWorkstationState
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/** A workstation row: a laptop icon with code scrolling on its screen while on (banana yellow). */
@Composable
fun WorkstationControl(
    ui: OfficeWorkstationUi,
    onToggle: () -> Unit,
) {
    Column {
        DeviceToggleRow(
            name = ui.name,
            isOn = ui.isOn,
            offline = ui.isOffline,
            onTint = AppColors.WorkstationOn,
            onToggle = onToggle,
            tintSwitch = true,
        ) { tint ->
            WorkstationIcon(
                on = ui.isOn,
                tint = tint,
                modifier = Modifier.size(Dimens.RowIconSize)
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Power Usage",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        PowerGraph(ui.powerHistoryPoints)
        Spacer(Modifier.height(8.dp))
        StatRow("Current Power", ui.currentPower)
        StatRow("Total Power", ui.totalPower)
    }
}

@Composable
private fun StatRow(
    label: String,
    valueText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(valueText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Custom laptop: screen + keyboard deck, with "code" lines scrolling up the screen while [on]. */
@Composable
private fun WorkstationIcon(
    on: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "ws")
    val scroll by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (on) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wsScroll",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.055f

        val sx0 = w * 0.18f
        val sy0 = h * 0.20f
        val sw = w * 0.64f
        val sh = h * 0.38f

        val deck = Path().apply {
            moveTo(w * 0.08f, h * 0.76f)
            lineTo(w * 0.92f, h * 0.76f)
            lineTo(sx0 + sw, sy0 + sh)
            lineTo(sx0, sy0 + sh)
            close()
        }
        drawPath(deck, color = tint)

        if (on) {
            val inset = stroke
            val left = sx0 + inset
            val right = sx0 + sw - inset
            val top = sy0 + inset
            val bottom = sy0 + sh - inset
            clipRect(left = left, top = top, right = right, bottom = bottom) {
                val widths = floatArrayOf(0.85f, 0.5f, 0.95f, 0.6f, 0.75f)
                val gap = (bottom - top) / 3f
                val shift = scroll * gap
                var i = 0
                var y = bottom + gap - shift
                while (y > top - gap) {
                    val lineLen = (right - left) * widths[i % widths.size]
                    drawLine(
                        color = tint,
                        start = Offset(left, y),
                        end = Offset(left + lineLen, y),
                        strokeWidth = h * 0.05f,
                    )
                    y -= gap
                    i++
                }
            }
        }

        drawRoundRect(
            color = tint,
            topLeft = Offset(sx0, sy0),
            size = Size(sw, sh),
            cornerRadius = CornerRadius(w * 0.04f),
            style = Stroke(width = stroke),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, heightDp = 640)
@Composable
private fun WorkstationControlPreview() =
    ControlPreview {
        WorkstationControl(previewWorkstation(isOn = true)) {}
        WorkstationControl(previewWorkstation(isOn = false, offline = true)) {}
    }

/** An offline workstation has no meters to read either, so its readouts dash and its chart is empty. */
private fun previewWorkstation(
    isOn: Boolean,
    offline: Boolean = false,
) = OfficeWorkstationUi(
    metadata = OfficeWorkstationMetadata(
        displayName = "Workstation",
        toggleEntityId = "switch.office_workstation",
        currentPowerEntityId = "sensor.office_workstation_power",
        totalPowerEntityId = "sensor.office_workstation_summation_delivered",
    ),
    state = OfficeWorkstationState(
        entityId = "switch.office_workstation",
        isOn = isOn,
        isOffline = offline,
        currentPower = if (offline) HistoricalEntityReading.Missing else HistoricalEntityReading(
            61.1,
            "W"
        ),
        totalPower = if (offline) HistoricalEntityReading.Missing else HistoricalEntityReading(
            34.8,
            "kWh"
        ),
        powerHistory = if (offline) emptyList() else previewHistory,
    ),
)
