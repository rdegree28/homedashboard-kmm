package com.degree.homedash.office

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.decimateHistory
import kotlin.math.roundToInt

/** A compact area/line chart of numeric history, echoing the old HA Power Usage graph. */
@Composable
fun PowerGraph(points: List<HistoryPoint>, modifier: Modifier = Modifier) {
    val lineColor = AppColors.Accent
    val fillColor = AppColors.Accent.copy(alpha = 0.2f)
    val gridColor = AppColors.GridLine

    Box(modifier.fillMaxWidth().height(Dimens.ChartHeight)) {
        if (points.size < 2) {
            Text(
                text = if (points.isEmpty()) "Loading…" else "Not enough data",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Box
        }

        // Downsample once per data change; a chart can't show more detail than its pixel width, and
        // this keeps the cached Path build below cheap even for a week of raw meter readings.
        val data = remember(points) { decimateHistory(points) }
        val maxV = remember(data) { data.maxOf { it.value }.coerceAtLeast(1.0) }
        val minT = data.first().timeSeconds
        val span = remember(data) { (data.last().timeSeconds - minT).coerceAtLeast(1.0) }

        // Build the area + line Paths in drawWithCache so they're re-tessellated only when the size or
        // the (decimated) data changes — not on every draw pass.
        Spacer(
            Modifier.fillMaxSize().drawWithCache {
                val w = size.width
                val h = size.height
                val strokeWidth = 2.dp.toPx()
                fun pointAt(p: HistoryPoint) = Offset(
                    x = (((p.timeSeconds - minT) / span) * w).toFloat(),
                    y = (h - (p.value / maxV) * h).toFloat(),
                )

                val area = Path().apply {
                    moveTo(0f, h)
                    data.forEach { val o = pointAt(it); lineTo(o.x, o.y) }
                    lineTo(w, h)
                    close()
                }
                val line = Path().apply {
                    data.forEachIndexed { i, p ->
                        val o = pointAt(p)
                        if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                    }
                }

                onDrawBehind {
                    drawLine(gridColor, Offset(0f, 1f), Offset(w, 1f), 1f)
                    drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), 1f)
                    drawPath(area, fillColor)
                    drawPath(line, lineColor, style = Stroke(width = strokeWidth))
                }
            },
        )

        Text(
            text = "${maxV.roundToInt()} W",
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
