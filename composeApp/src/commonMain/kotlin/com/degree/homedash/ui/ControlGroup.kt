package com.degree.homedash.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.core.DeviceControl
import com.degree.homedash.core.device.DeviceUi

/**
 * A titled group of related controls with arbitrary [content] — the standard section container for
 * dashboards. The group is wrapped in a [Card] with the title inside it. Use this for sections that
 * mix control types or embed extras (graphs, selectors); for a homogeneous device list that can
 * become a card grid, use the [devices] overload below.
 *
 * [titleOutsideCard] lifts the title above the card instead, the way the card-grid groups label
 * themselves — so a slot group can sit next to them without its heading looking a level deeper.
 */
@Composable
fun ControlGroup(
    title: String,
    titleOutsideCard: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (titleOutsideCard) GroupTitle(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!titleOutsideCard) GroupTitle(title)
                content()
            }
        }
    }
}

/**
 * A titled group backed by a list of [devices], each rendered by [DeviceControl]. The group drops the
 * [Card] wrapper and lays its cards out as a 2-column grid with the title above them, so the cards
 * carry the surface themselves. [empty] shows when [devices] is empty.
 */
@Composable
fun ControlGroup(
    title: String,
    devices: List<DeviceUi>,
    empty: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GroupTitle(title)
        CardGrid(devices)
    }
}

/**
 * The 2-column card grid. Cards are packed into rows by [packCardRows], but each card is a
 * [movableContentOf] keyed by entity id and tagged with [animateBounds] inside a shared
 * [LookaheadScope]. So when a fan toggles and its [cardSpan] flips 1↔2, the card keeps its identity
 * as it moves to a new row and *animates* its size + position change instead of snapping — the fan
 * glides wider and its neighbours slide to their new slots. The `weight` on the wrapping [Box] gives
 * each card its column width; `fillMaxWidth` + `animateBounds` on the card animate toward it.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CardGrid(
    devices: List<DeviceUi>,
) {
    LookaheadScope {
        val lookaheadScope = this
        // One stable movable slot per device id, reused across state pushes and row moves. The latest
        // DeviceUi is passed in at call time, so slots don't need recreating when values change.
        val slots = remember { mutableMapOf<String, @Composable (DeviceUi) -> Unit>() }
        slots.keys.retainAll(devices.mapTo(HashSet()) { it.id })
        devices.forEach { entity ->
            slots.getOrPut(entity.id) {
                movableContentOf { latest: DeviceUi ->
                    DeviceControl(
                        device = latest,
                        modifier = Modifier.fillMaxWidth().animateBounds(lookaheadScope),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            packCardRows(devices, columns = 2).forEach { rowEntities ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var used = 0
                    rowEntities.forEach { entity ->
                        val span = entity.cardSpan.coerceIn(1, 2)
                        Box(Modifier.weight(span.toFloat())) {
                            slots.getValue(entity.id)(entity)
                        }
                        used += span
                    }
                    // Pad a short row so its cards keep the grid's column width.
                    repeat(2 - used) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * Greedily packs [entities] into grid rows whose card spans sum to at most [columns]. A span-2 card
 * (see [cardSpan]) starts a fresh row and fills it; span-1 cards pair up. Preserves list order.
 */
private fun packCardRows(devices: List<DeviceUi>, columns: Int): List<List<DeviceUi>> {
    val rows = mutableListOf<List<DeviceUi>>()
    var current = mutableListOf<DeviceUi>()
    var width = 0
    for (device in devices) {
        val span = device.cardSpan.coerceIn(1, columns)
        if (width + span > columns && current.isNotEmpty()) {
            rows.add(current)
            current = mutableListOf()
            width = 0
        }
        current.add(device)
        width += span
    }
    if (current.isNotEmpty()) rows.add(current)
    return rows
}

@Composable
private fun GroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}
