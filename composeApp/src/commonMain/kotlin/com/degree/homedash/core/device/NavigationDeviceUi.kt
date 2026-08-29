package com.degree.homedash.core.device

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata

/**
 * Render state for a launcher tile.
 *
 * Like [TriggerDeviceUi] it has no live state — the metadata is the whole card. Unlike every other
 * device it carries no behavior either: opening a dashboard is navigation, which belongs to whoever
 * owns the back stack, so the Home screen renders these itself and supplies the click. [DeviceControl]
 * has nothing it could usefully do with one.
 */
@Immutable
data class NavigationDeviceUi(
    val metadata: NavigationMetadata,
) : DeviceUi {

    /** Synthetic — a launcher card has no Home Assistant entity; it exists to key the list. */
    override val id: String get() = metadata.entityId

    /** Half width: the launcher lays these out two to a row. */
    override val cardSpan: Int get() = 1

    val name: String get() = metadata.displayName

    /** The dashboard this tile opens. */
    val destination: NavigationMetadata.NavigationTarget get() = metadata.destination
}
