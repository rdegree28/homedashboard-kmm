package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.EntityMetadata

data class LightEntityUi(
    override val id: String,
    val name: String,
    val isOn: Boolean,
    val offline: Boolean,

    val onToggle: suspend () -> Unit,
) : ExpEntityUi {

    override val cardSpan: Int
        get() = 1
}