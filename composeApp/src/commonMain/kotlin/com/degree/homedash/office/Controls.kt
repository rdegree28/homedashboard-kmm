package com.degree.homedash.office

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState

private val AmberOn = Color(0xFFFFC107)
private val FanOn = Color(0xFF4C8DFF)

/** A light entity row: a bulb icon (amber when on) + name + toggle. */
@Composable
fun LightControl(
    name: String,
    entity: EntityState?,
    icon: ImageVector,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, icon, AmberOn, onToggle)

/** A fan entity row: a fan icon (blue when on) + name + toggle. */
@Composable
fun FanControl(
    name: String,
    entity: EntityState?,
    icon: ImageVector,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, icon, FanOn, onToggle)

@Composable
private fun EntityToggleRow(
    name: String,
    entity: EntityState?,
    icon: ImageVector,
    onTint: Color,
    onToggle: () -> Unit,
) {
    val isOn = entity?.isOn == true
    val available = entity != null && !entity.isUnavailable
    val tint = if (isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        Switch(
            checked = isOn,
            enabled = available,
            onCheckedChange = { onToggle() },
        )
    }
}
