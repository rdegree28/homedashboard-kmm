package com.degree.homedash.core.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.core.util.ControlPreview
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * Read-only door card: the door glyph pinned top-left with Open/Closed above the [label], centered.
 *
 * Mirrors [ClimateControl]'s tile shape so the two line up in a shared grid; it can't reuse it because
 * the door icon is drawn on a Canvas rather than being an `ImageVector`. Non-interactive — a contact
 * sensor has nothing to drive.
 */
@Composable
internal fun DoorControl(
    label: String,
    statusText: String,
    open: Boolean,
    unavailable: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = if (unavailable) Color.White.copy(alpha = 0.3f) else Color.White

    Surface(
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = AppColors.CardBackground,
        shadowElevation = Dimens.CardElevation,
        modifier = modifier.height(Dimens.EntityCardHeight),
    ) {
        Box(Modifier.fillMaxSize().padding(Dimens.EntityCardPadding)) {
            DoorIcon(
                open = open,
                tint = tint,
                modifier = Modifier.align(Alignment.TopStart).size(Dimens.RowIconSize),
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
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
private fun DoorControlPreview() = ControlPreview {
    DoorControl(
        "Office Door", "Open", open = true, unavailable = false,
        modifier = Modifier.size(180.dp, Dimens.EntityCardHeight)
    )
    DoorControl(
        "Front Door", "Closed", open = false, unavailable = false,
        modifier = Modifier.size(180.dp, Dimens.EntityCardHeight)
    )
    DoorControl(
        "Garage", "—", open = false, unavailable = true,
        modifier = Modifier.size(180.dp, Dimens.EntityCardHeight)
    )
}

/** Door icon: hollow outline when [open], solid slab when closed. */
@Composable
internal fun DoorIcon(
    open: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
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
