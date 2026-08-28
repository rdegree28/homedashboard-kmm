package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.TriggerDeviceMetadata
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Render state for a scene/script card.
 *
 * Alone among the devices it has no state at all: a trigger fires a service and reports nothing back,
 * so the metadata is the whole card. The press is the metadata's too — see
 * [TriggerDeviceMetadata.onActivate].
 */
@Immutable
data class TriggerDeviceUi(
    private val metadata: TriggerDeviceMetadata,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    override val cardSpan: Int get() = 1

    val name: String get() = metadata.displayName

    fun onActivate(repo: ExpHomeAssistantRepo) = metadata.onActivate(repo)
}
