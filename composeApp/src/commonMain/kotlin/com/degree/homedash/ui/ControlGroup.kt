package com.degree.homedash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.degree.homedash.controls.ControlLayout
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.EntityControl
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.hasCard

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
    Card(modifier = Modifier.fillMaxWidth()) {
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
            entities.chunked(2).forEach { rowEntities ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowEntities.forEach { entity ->
                        EntityControl(entity, ControlLayout.Card, onAction, Modifier.weight(1f))
                    }
                    // Pad the final row so cards keep a uniform width.
                    repeat(2 - rowEntities.size) { Spacer(Modifier.weight(1f)) }
                }
            }
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

@Composable
private fun GroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}
