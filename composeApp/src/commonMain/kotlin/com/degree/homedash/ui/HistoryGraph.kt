package com.degree.homedash.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.HistoryPoint
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A compact area/line chart of numeric [points] over time, scaled 0..max. Generic over the series
 * colour and the max-value label so different dashboards (power, moisture, …) can reuse it.
 */
@Composable
fun HistoryGraph(
    points: List<HistoryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = AppColors.Accent,
    fillColor: Color = lineColor.copy(alpha = 0.2f),
    height: Dp = Dimens.ChartHeight,
    /** Fixed top of the value axis; when null the chart auto-scales to the data's max. */
    maxValue: Double? = null,
    maxLabel: (Double) -> String = { it.roundToInt().toString() },
    /** When set, the line is drawn segment-by-segment colored by each segment's value. */
    colorForValue: ((Double) -> Color)? = null,
    /** When true, time labels (hour/weekday/date/month, chosen by span) are drawn under the chart. */
    showTimeAxis: Boolean = false,
) {
    val gridColor = AppColors.GridLine
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(height)) {
            if (points.size < 2) {
                Text(
                    text = if (points.isEmpty()) "Loading…" else "Not enough data",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = axisColor,
                )
                return@Box
            }

            val top = (maxValue ?: points.maxOf { it.value }).coerceAtLeast(1.0)
            val minT = points.first().timeSeconds
            val maxT = points.last().timeSeconds
            val span = (maxT - minT).coerceAtLeast(1.0)

            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                fun pointAt(p: HistoryPoint) = Offset(
                    x = (((p.timeSeconds - minT) / span) * w).toFloat(),
                    y = (h - (p.value / top).coerceIn(0.0, 1.0) * h).toFloat(),
                )

                // Axis gridlines: top (max), middle, baseline (0).
                drawLine(gridColor, Offset(0f, 1f), Offset(w, 1f), 1f)
                drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), 1f)
                drawLine(gridColor, Offset(0f, h - 1f), Offset(w, h - 1f), 1f)

                val strokeWidth = 2.dp.toPx()
                if (colorForValue == null) {
                    val area = Path().apply {
                        moveTo(0f, h)
                        points.forEach { val o = pointAt(it); lineTo(o.x, o.y) }
                        lineTo(w, h)
                        close()
                    }
                    drawPath(area, fillColor)

                    val line = Path().apply {
                        points.forEachIndexed { i, p ->
                            val o = pointAt(p)
                            if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                        }
                    }
                    drawPath(line, lineColor, style = Stroke(width = strokeWidth))
                } else {
                    // Per segment: shade the slice down to the baseline and draw the line in the same
                    // value-based color, so dry/healthy stretches read at a glance.
                    for (i in 1 until points.size) {
                        val a = pointAt(points[i - 1])
                        val b = pointAt(points[i])
                        val color = colorForValue((points[i - 1].value + points[i].value) / 2.0)

                        val slice = Path().apply {
                            moveTo(a.x, h)
                            lineTo(a.x, a.y)
                            lineTo(b.x, b.y)
                            lineTo(b.x, h)
                            close()
                        }
                        drawPath(slice, color.copy(alpha = 0.22f))
                        drawLine(color, a, b, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    }
                }
            }

            Text(
                text = maxLabel(top),
                modifier = Modifier.align(Alignment.TopStart),
                style = MaterialTheme.typography.labelSmall,
                color = axisColor,
            )
            Text(
                text = maxLabel(0.0),
                modifier = Modifier.align(Alignment.BottomStart),
                style = MaterialTheme.typography.labelSmall,
                color = axisColor,
            )
        }

        if (showTimeAxis && points.size >= 2) {
            TimeAxis(minT = points.first().timeSeconds, maxT = points.last().timeSeconds, color = axisColor)
        }
    }
}

/**
 * Downsamples [points] to roughly [maxPoints] while preserving the visual envelope: the series is
 * split into evenly-sized buckets and each contributes its min- and max-value point (in time order),
 * so spikes and dips survive. A chart can't render more than ~1 point per horizontal pixel, so this
 * cuts the per-draw Path cost from thousands of segments to a few hundred with no visible change.
 * Returns [points] unchanged when it's already small enough. First/last points are always kept.
 */
internal fun decimateHistory(points: List<HistoryPoint>, maxPoints: Int = 512): List<HistoryPoint> {
    if (points.size <= maxPoints) return points
    val buckets = maxPoints / 2
    val n = points.size
    val out = ArrayList<HistoryPoint>(maxPoints + 2)
    out.add(points.first())
    for (b in 0 until buckets) {
        val start = (b.toLong() * n / buckets).toInt()
        val end = ((b + 1).toLong() * n / buckets).toInt()
        if (start >= end) continue
        var lo = points[start]
        var hi = points[start]
        for (i in start until end) {
            val v = points[i].value
            if (v < lo.value) lo = points[i]
            if (v > hi.value) hi = points[i]
        }
        if (lo.timeSeconds <= hi.timeSeconds) { out.add(lo); out.add(hi) } else { out.add(hi); out.add(lo) }
    }
    out.add(points.last())
    return out
}

/** One [bucketHistory] time window: its start time plus the time-weighted [average] and [peak] value. */
internal class HistoryBucket(val timeSeconds: Double, val average: Double, val peak: Double)

/**
 * Buckets [points] into fixed [bucketSeconds]-wide time windows (e.g. hourly over a week), returning
 * each non-empty window's start time, time-weighted average, and peak value — a smooth, low-point
 * series. The average is *time-weighted* because the readings are stepwise (a value holds until the
 * next sample), so a segment contributes to a bucket in proportion to how long it overlaps it: a value
 * that persists counts more than a brief spike, and clusters of state-changes don't bias the result.
 * The peak is the highest value present at any moment in the window. Empty windows are omitted (a
 * step-after chart holds the prior value across the gap). Returns one bucket per point when there are
 * fewer than two.
 */
internal fun bucketHistory(points: List<HistoryPoint>, bucketSeconds: Double = 3600.0): List<HistoryBucket> {
    if (points.size < 2) return points.map { HistoryBucket(it.timeSeconds, it.value, it.value) }
    val minT = points.first().timeSeconds
    val maxT = points.last().timeSeconds
    val span = (maxT - minT).coerceAtLeast(1.0)
    val dur = bucketSeconds.coerceAtLeast(1.0)
    val n = (ceil(span / dur).toInt()).coerceAtLeast(1)

    val weight = DoubleArray(n)   // total time each bucket has data for
    val weighted = DoubleArray(n) // Σ value·overlap per bucket
    val peak = DoubleArray(n) { Double.NEGATIVE_INFINITY }

    for (i in 0 until points.size - 1) {
        val v = points[i].value
        var t = points[i].timeSeconds
        val segEnd = points[i + 1].timeSeconds
        while (t < segEnd) {
            val b = (((t - minT) / dur).toInt()).coerceIn(0, n - 1)
            val chunkEnd = minOf(segEnd, minT + (b + 1) * dur)
            val overlap = chunkEnd - t
            if (overlap <= 0.0) break
            weight[b] += overlap
            weighted[b] += v * overlap
            if (v > peak[b]) peak[b] = v
            t = chunkEnd
        }
    }

    val out = ArrayList<HistoryBucket>(n)
    for (b in 0 until n) {
        if (weight[b] > 0.0) out.add(HistoryBucket(minT + b * dur, weighted[b] / weight[b], peak[b]))
    }
    return out
}

/** Weekday labels positioned under the chart at each local-midnight boundary within the range. */
@Composable
private fun TimeAxis(minT: Double, maxT: Double, color: Color) {
    val ticks = remember(minT, maxT) { timeAxisTicks(minT, maxT) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(16.dp)) {
        val widthPx = constraints.maxWidth
        ticks.forEach { tick ->
            Text(
                text = tick.label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.offset { IntOffset((tick.fraction * widthPx).roundToInt(), 0) },
            )
        }
    }
}

private data class AxisTick(val fraction: Float, val label: String)

private val WEEKDAYS = arrayOf("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed") // epoch day 0 == Thursday
private val MONTHS =
    arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/**
 * Adaptive x-axis ticks chosen by the time span: 6-hour marks for ≤~2 days, weekdays for ≤~2 weeks,
 * month/day dates for ≤~3 months, otherwise month names. All in local time via the device's UTC
 * offset (no calendar dependency).
 */
private fun timeAxisTicks(minT: Double, maxT: Double): List<AxisTick> {
    val offset = localUtcOffsetSeconds()
    val spanDays = (maxT - minT) / 86_400.0
    return when {
        spanDays <= 2.5 -> hourTicks(minT, maxT, offset, stepHours = 6)
        spanDays <= 16 -> dayTicks(minT, maxT, offset, stepDays = 1) { epochDay ->
            WEEKDAYS[(((epochDay % 7) + 7) % 7).toInt()]
        }
        spanDays <= 100 -> dayTicks(minT, maxT, offset, stepDays = maxOf(1, (spanDays / 7).toInt())) { epochDay ->
            val (_, m, d) = civilFromDays(epochDay)
            "$m/$d"
        }
        else -> monthTicks(minT, maxT, offset)
    }
}

private fun hourTicks(minT: Double, maxT: Double, offset: Int, stepHours: Int): List<AxisTick> {
    val span = (maxT - minT).coerceAtLeast(1.0)
    val step = stepHours * 3600.0
    val ticks = mutableListOf<AxisTick>()
    var localSlot = ceil((minT + offset) / step) * step
    while (true) {
        val tUtc = localSlot - offset
        if (tUtc > maxT) break
        val hour = (((localSlot / 3600).toLong() % 24) + 24) % 24
        ticks += AxisTick(((tUtc - minT) / span).toFloat(), hourLabel(hour.toInt()))
        localSlot += step
    }
    return ticks
}

private inline fun dayTicks(
    minT: Double,
    maxT: Double,
    offset: Int,
    stepDays: Int,
    label: (epochDay: Long) -> String,
): List<AxisTick> {
    val span = (maxT - minT).coerceAtLeast(1.0)
    val ticks = mutableListOf<AxisTick>()
    var dayIndex = floor((minT + offset) / 86_400.0).toLong()
    while (true) {
        val midnightUtc = dayIndex * 86_400.0 - offset
        if (midnightUtc > maxT) break
        if (midnightUtc >= minT) ticks += AxisTick(((midnightUtc - minT) / span).toFloat(), label(dayIndex))
        dayIndex += stepDays
    }
    return ticks
}

private fun monthTicks(minT: Double, maxT: Double, offset: Int): List<AxisTick> {
    val span = (maxT - minT).coerceAtLeast(1.0)
    val ticks = mutableListOf<AxisTick>()
    var (year, month, _) = civilFromDays(floor((minT + offset) / 86_400.0).toLong())
    while (true) {
        val tUtc = daysFromCivil(year, month, 1) * 86_400.0 - offset
        if (tUtc > maxT) break
        if (tUtc >= minT) ticks += AxisTick(((tUtc - minT) / span).toFloat(), MONTHS[month - 1])
        if (month == 12) { month = 1; year++ } else month++
    }
    return ticks
}

private fun hourLabel(h: Int): String = when {
    h == 0 -> "12a"
    h < 12 -> "${h}a"
    h == 12 -> "12p"
    else -> "${h - 12}p"
}

/** (year, month, day) from days-since-1970 (Howard Hinnant's civil-from-days). */
private fun civilFromDays(days: Long): Triple<Int, Int, Int> {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    return Triple((if (m <= 2) y + 1 else y).toInt(), m, d)
}

/** days-since-1970 for a (year, month, day) (inverse of [civilFromDays]). */
private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = (if (month <= 2) year - 1 else year).toLong()
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val mp = if (month > 2) month - 3 else month + 9
    val doy = (153L * mp + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}
