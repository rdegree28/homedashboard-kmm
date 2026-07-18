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
import com.degree.homedash.ui.HistoryBucket
import com.degree.homedash.ui.bucketHistory
import kotlin.math.roundToInt

/** A compact area/line chart of numeric history, echoing the old HA Power Usage graph. */
@Composable
fun PowerGraph(points: List<HistoryPoint>, modifier: Modifier = Modifier) {
    val lineColor = AppColors.Accent
    val fillColor = AppColors.Accent.copy(alpha = 0.2f)
    val peakColor = AppColors.PowerPeak.copy(alpha = 0.3f)
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

        // Reduce the raw week of readings to hourly buckets (time-weighted average + peak) once per data
        // change: fewer, smoother points so the step chart reads clearly instead of a dense comb of spikes.
        val buckets = remember(points) { bucketHistory(points, bucketSeconds = 3600.0) }
        val maxV = remember(buckets) { (buckets.maxOfOrNull { it.peak } ?: 1.0).coerceAtLeast(1.0) }
        val minT = points.first().timeSeconds
        val span = (points.last().timeSeconds - minT).coerceAtLeast(1.0)

        // Build the step Paths in drawWithCache so they're re-tessellated only when the size or the
        // bucketed data changes — not on every draw pass.
        Spacer(
            Modifier.fillMaxSize().drawWithCache {
                val w = size.width
                val h = size.height
                val strokeWidth = 2.dp.toPx()

                // Bucket values → screen offsets, plus a terminal point holding the last value to the
                // right edge so the final hour's step spans the full range.
                fun offsets(value: (HistoryBucket) -> Double): List<Offset> {
                    val pts = buckets.map { b ->
                        Offset(
                            x = (((b.timeSeconds - minT) / span) * w).toFloat(),
                            y = (h - (value(b) / maxV) * h).toFloat(),
                        )
                    }
                    return if (pts.isEmpty()) pts else pts + Offset(w, pts.last().y)
                }

                // Stepwise reading: hold each value until the next sample, then jump vertically
                // (step-after) — never a diagonal ramp. A rise from 0 to Y stays at 0, then steps up.
                fun stepLine(offs: List<Offset>) = Path().apply {
                    offs.forEachIndexed { i, o ->
                        if (i == 0) moveTo(o.x, o.y) else { lineTo(o.x, offs[i - 1].y); lineTo(o.x, o.y) }
                    }
                }

                val avgOffs = offsets { it.average }
                val peakOffs = offsets { it.peak }

                val area = Path().apply {
                    moveTo(0f, h)
                    avgOffs.forEachIndexed { i, o ->
                        if (i == 0) lineTo(o.x, o.y) else { lineTo(o.x, avgOffs[i - 1].y); lineTo(o.x, o.y) }
                    }
                    lineTo(w, h)
                    close()
                }
                val avgLine = stepLine(avgOffs)
                val peakLine = stepLine(peakOffs)

                onDrawBehind {
                    drawLine(gridColor, Offset(0f, 1f), Offset(w, 1f), 1f)
                    drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), 1f)
                    drawPath(area, fillColor)
                    drawPath(peakLine, peakColor, style = Stroke(width = strokeWidth))
                    drawPath(avgLine, lineColor, style = Stroke(width = strokeWidth))
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
