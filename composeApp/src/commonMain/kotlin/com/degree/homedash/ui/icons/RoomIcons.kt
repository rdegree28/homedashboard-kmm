package com.degree.homedash.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata.RoomIcon

/**
 * Hand-drawn room glyphs, replacing the Material icons the launcher used to borrow. Filled
 * silhouettes on the same 24dp/24-unit grid as the Material chrome they sit beside (back arrow,
 * gear), so weights match.
 *
 * Named for what they draw, matching the [RoomIcon] tokens the metadata declares. Each is built from
 * solid black paths, which is what lets `Icon(tint = …)` recolour them.
 */
object RoomIcons {

    /** A monitor on a desk. */
    val Desk: ImageVector by lazy {
        roomIcon("Desk") {
            path(fill = SolidColor(Color.Black)) { roundedRect(3f, 3.5f, 21f, 14.5f, 1.5f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(10.4f, 14.5f, 13.6f, 17.4f, 0.3f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(2f, 17.4f, 22f, 20f, 0.8f) }
        }
    }

    /** A two-leafed seedling in a tapered pot. */
    val Plant: ImageVector by lazy {
        roomIcon("Plant") {
            // Pot: a trapezoid, wider at the rim.
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.5f, 14.6f)
                lineTo(17.5f, 14.6f)
                lineTo(16f, 20.6f)
                lineTo(8f, 20.6f)
                close()
            }
            path(fill = SolidColor(Color.Black)) { roundedRect(11.3f, 7.5f, 12.7f, 15f, 0.6f) }
            // Leaves, mirrored about the stem.
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 13.2f)
                curveTo(12f, 9f, 9.5f, 5.6f, 5.6f, 4.6f)
                curveTo(4.7f, 8.9f, 7.4f, 12.5f, 12f, 13.2f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 13.2f)
                curveTo(12f, 9f, 14.5f, 5.6f, 18.4f, 4.6f)
                curveTo(19.3f, 8.9f, 16.6f, 12.5f, 12f, 13.2f)
                close()
            }
        }
    }

    /** A sofa: backrest, seat, and two feet. */
    val Sofa: ImageVector by lazy {
        roomIcon("Sofa") {
            path(fill = SolidColor(Color.Black)) { roundedRect(4f, 6f, 20f, 13f, 2f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(2f, 11.5f, 22f, 17.5f, 2f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(4.3f, 17.5f, 6.6f, 20.3f, 0.4f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(17.4f, 17.5f, 19.7f, 20.3f, 0.4f) }
        }
    }

    /** A bed seen from the side: headboard, pillow, mattress, feet. */
    val Bed: ImageVector by lazy {
        roomIcon("Bed") {
            path(fill = SolidColor(Color.Black)) { roundedRect(2f, 5.5f, 4.6f, 18.6f, 1.2f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(5.4f, 8.8f, 10.6f, 12.8f, 1.3f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(4.6f, 12.6f, 22f, 17.2f, 1.6f) }
            path(fill = SolidColor(Color.Black)) { roundedRect(19.6f, 17.2f, 22f, 19.9f, 0.4f) }
        }
    }

    /** A paw print: four toes over a pad. */
    val Paw: ImageVector by lazy {
        roomIcon("Paw") {
            // Toe spacing is deliberate: any wider and adjacent toes merge into one blob at 24dp.
            path(fill = SolidColor(Color.Black)) { ellipse(6.3f, 9.4f, 1.75f, 2.4f) }
            path(fill = SolidColor(Color.Black)) { ellipse(10f, 6.5f, 1.7f, 2.5f) }
            path(fill = SolidColor(Color.Black)) { ellipse(14f, 6.5f, 1.7f, 2.5f) }
            path(fill = SolidColor(Color.Black)) { ellipse(17.7f, 9.4f, 1.75f, 2.4f) }
            path(fill = SolidColor(Color.Black)) { ellipse(12f, 15.9f, 4.5f, 3.9f) }
        }
    }
}

/** Resolves the icon token a card declares (see [NavigationMetadata.RoomIcon]) to its drawing. */
fun roomIcon(icon: RoomIcon): ImageVector = when (icon) {
    RoomIcon.Desk -> RoomIcons.Desk
    RoomIcon.Plant -> RoomIcons.Plant
    RoomIcon.Sofa -> RoomIcons.Sofa
    RoomIcon.Bed -> RoomIcons.Bed
    RoomIcon.Paw -> RoomIcons.Paw
}

private fun roomIcon(name: String, paths: ImageVector.Builder.() -> Unit): ImageVector =
    iconVector(name, paths)
