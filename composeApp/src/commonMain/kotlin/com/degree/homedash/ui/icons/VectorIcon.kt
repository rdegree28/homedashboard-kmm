package com.degree.homedash.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp

/**
 * Shared scaffolding for the app's hand-drawn glyphs ([RoomIcons], [ControlIcons]).
 *
 * Everything is drawn on the same 24dp/24-unit grid as the Material chrome the icons sit beside
 * (back arrow, gear), so weights match, and in black so `Icon(tint = …)` can recolour it.
 */
internal fun iconVector(name: String, paths: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(paths).build()

/** Circular-arc constant: a Bézier control point sits this fraction of the radius from the corner. */
internal const val KAPPA = 0.4477f

/** An axis-aligned rectangle with uniform corner rounding. */
internal fun PathBuilder.roundedRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
) {
    val c = radius * KAPPA
    moveTo(left + radius, top)
    lineTo(right - radius, top)
    curveTo(right - c, top, right, top + c, right, top + radius)
    lineTo(right, bottom - radius)
    curveTo(right, bottom - c, right - c, bottom, right - radius, bottom)
    lineTo(left + radius, bottom)
    curveTo(left + c, bottom, left, bottom - c, left, bottom - radius)
    lineTo(left, top + radius)
    curveTo(left, top + c, left + c, top, left + radius, top)
    close()
}

/** An axis-aligned ellipse, approximated with the usual four cubic segments. */
internal fun PathBuilder.ellipse(cx: Float, cy: Float, rx: Float, ry: Float) {
    val kx = rx * (1f - KAPPA)
    val ky = ry * (1f - KAPPA)
    moveTo(cx - rx, cy)
    curveTo(cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry)
    curveTo(cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy)
    curveTo(cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry)
    curveTo(cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy)
    close()
}

/** A circle — [ellipse] with equal radii, which is most of what the glyphs actually want. */
internal fun PathBuilder.circle(cx: Float, cy: Float, radius: Float) = ellipse(cx, cy, radius, radius)
