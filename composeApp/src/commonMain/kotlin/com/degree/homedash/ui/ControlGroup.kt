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
import com.degree.homedash.controls.ControlLayout
import com.degree.homedash.controls.ControlPreview
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.EntityControl
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.cardSpan
import com.degree.homedash.controls.entityId
import com.degree.homedash.controls.hasCard
import com.degree.homedash.controls.previewFanUi
import com.degree.homedash.controls.previewLight

/**
 * A titled group of related controls with arbitrary [content] — the standard section container for
 * dashboards. The group is wrapped in a [Card] with the title inside it. Use this for sections that
 * mix control types or embed extras (graphs, selectors); for a homogeneous entity list that can
 * become a card grid, use the [entities] overload below.
 */
@Composable
fun ControlGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GroupTitle(title)
            content()
        }
    }
}

/**
 * A titled group backed by a list of [entities], rendered via [EntityControl] with interaction routed
 * through [onAction]. When [useCardUis] is true and every entity has a card form, the group drops its
 * wrapper and lays the cards out as a 2-column grid (title above); otherwise it renders rows inside the
 * standard [Card] wrapper (row-only groups keep their wrapper). [empty] shows when [entities] is empty.
 */
@Composable
fun ControlGroup(
    title: String,
    entities: List<EntityUi>,
    useCardUis: Boolean = false,
    onAction: (EntityAction) -> Unit,
    empty: @Composable () -> Unit = {},
) {
    val asCards = useCardUis && entities.isNotEmpty() && entities.all { it.hasCard() }
    if (asCards) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GroupTitle(title)
            CardGrid(entities, onAction)
        }
    } else {
        ControlGroup(title) {
            if (entities.isEmpty()) {
                empty()
            } else {
                entities.forEach { EntityControl(it, ControlLayout.Row, onAction) }
            }
        }
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
    entities: List<EntityUi>,
    onAction: (EntityAction) -> Unit,
) {
    LookaheadScope {
        val lookaheadScope = this
        // One stable movable slot per entity id, reused across state pushes and row moves. The latest
        // EntityUi is passed in at call time, so slots don't need recreating when values change.
        val slots = remember { mutableMapOf<String, @Composable (EntityUi) -> Unit>() }
        slots.keys.retainAll(entities.mapTo(HashSet()) { it.entityId })
        entities.forEach { entity ->
            slots.getOrPut(entity.entityId) {
                movableContentOf { latest: EntityUi ->
                    EntityControl(
                        entity = latest,
                        layout = ControlLayout.Card,
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth().animateBounds(lookaheadScope),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            packCardRows(entities, columns = 2).forEach { rowEntities ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var used = 0
                    rowEntities.forEach { entity ->
                        val span = entity.cardSpan().coerceIn(1, 2)
                        Box(Modifier.weight(span.toFloat())) {
                            slots.getValue(entity.entityId)(entity)
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
private fun packCardRows(entities: List<EntityUi>, columns: Int): List<List<EntityUi>> {
    val rows = mutableListOf<List<EntityUi>>()
    var current = mutableListOf<EntityUi>()
    var width = 0
    for (entity in entities) {
        val span = entity.cardSpan().coerceIn(1, columns)
        if (width + span > columns && current.isNotEmpty()) {
            rows.add(current)
            current = mutableListOf()
            width = 0
        }
        current.add(entity)
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

/**
 * Exercises the flex packing: an ON multi-level fan takes its own full-width row (with slider) while
 * lights and the OFF fan pack two-per-row. Order-preserving greedy fill, so a 2-wide card flushes the
 * current row. Toggling the ceiling fan on/off in the app reflows this layout.
 */
@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun ControlGroupFlexPreview() = ControlPreview {
    ControlGroup(
        title = "Mixed",
        entities = listOf(
            previewLight("Lamp", isOn = true),
            previewLight("Desk", isOn = false),
            previewFanUi("Ceiling", isOn = true, percentage = 75, levelCount = 12),
            previewFanUi("Tower", isOn = false),
            previewLight("Reading", isOn = true),
        ),
        useCardUis = true,
        onAction = {},
    )
}
