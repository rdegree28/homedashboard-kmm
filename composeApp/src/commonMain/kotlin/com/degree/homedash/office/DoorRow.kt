package com.degree.homedash.office

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState

/** A read-only door row: white door icon (hollow when open, solid when closed) + Open/Closed. */
@Composable
fun DoorRow(label: String, entity: EntityState?) {
    val unavailable = entity == null || entity.isUnavailable
    val open = entity?.state == "on" // device_class opening: on = open
    val status = when {
        unavailable -> "—"
        open -> "Open"
        else -> "Closed"
    }
    val tint = if (unavailable) Color.White.copy(alpha = 0.3f) else Color.White

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DoorIcon(open = open, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            text = status,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Door icon: hollow outline when [open], solid slab when closed. */
@Composable
private fun DoorIcon(open: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val left = w * 0.30f
        val top = h * 0.15f
        val right = w * 0.70f
        val bottom = h * 0.85f
        val corner = CornerRadius(w * 0.06f)
        val knob = Offset(right - w * 0.10f, (top + bottom) / 2f)
        val knobR = w * 0.045f

        if (open) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = corner,
                style = Stroke(width = w * 0.08f),
            )
            drawCircle(color = tint, radius = knobR, center = knob)
        } else {
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = corner,
            )
            drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = knobR, center = knob)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun DoorRowPreview() = ControlPreview {
    DoorRow("Open", previewEntity("on"))
    DoorRow("Closed", previewEntity("off"))
    DoorRow("Unavailable", previewEntity("unavailable"))
}
