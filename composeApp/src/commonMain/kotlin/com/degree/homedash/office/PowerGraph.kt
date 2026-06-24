package com.degree.homedash.office

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.HistoryPoint
import kotlin.math.roundToInt

/** A compact area/line chart of numeric history, echoing the old HA Power Usage graph. */
@Composable
fun PowerGraph(points: List<HistoryPoint>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF4C8DFF)
    val fillColor = Color(0x334C8DFF)
    val gridColor = Color(0x22FFFFFF)

    Box(modifier.fillMaxWidth().height(150.dp)) {
        if (points.size < 2) {
            Text(
                text = if (points.isEmpty()) "Loading…" else "Not enough data",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Box
        }

        val maxV = points.maxOf { it.value }.coerceAtLeast(1.0)
        val minT = points.first().timeSeconds
        val maxT = points.last().timeSeconds
        val span = (maxT - minT).coerceAtLeast(1.0)

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun pointAt(p: HistoryPoint) = Offset(
                x = (((p.timeSeconds - minT) / span) * w).toFloat(),
                y = (h - (p.value / maxV) * h).toFloat(),
            )

            // Horizontal gridlines (max, mid).
            drawLine(gridColor, Offset(0f, 1f), Offset(w, 1f), 1f)
            drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), 1f)

            // Filled area under the curve.
            val area = Path().apply {
                moveTo(0f, h)
                points.forEach { val o = pointAt(it); lineTo(o.x, o.y) }
                lineTo(w, h)
                close()
            }
            drawPath(area, fillColor)

            // Line on top.
            val line = Path().apply {
                points.forEachIndexed { i, p ->
                    val o = pointAt(p)
                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                }
            }
            drawPath(line, lineColor, style = Stroke(width = 2.dp.toPx()))
        }

        Text(
            text = "${maxV.roundToInt()} W",
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
