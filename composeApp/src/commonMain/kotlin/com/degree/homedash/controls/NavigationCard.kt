package com.degree.homedash.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.entity.NavigationMetadata.NavigationTarget
import com.degree.homedash.ui.icons.roomIcon
import com.degree.homedash.ui.roomTint

/**
 * A full-width launcher card: destination icon, label, and a trailing chevron. Tapping it opens the
 * dashboard named by the entity's destination.
 */
@Composable
fun NavigationCard(
    ui: EntityUi.Navigation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val target = ui.metadata.destination
    Card(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = roomIcon(ui.metadata.icon),
                contentDescription = null,
                tint = roomTint(ui.metadata.tint),
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = ui.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(widthDp = 380)
@Composable
private fun NavigationCardPreview() = ControlPreview {
    NavigationTarget.entries.forEach { target ->
        NavigationCard(previewNavigation(target), onClick = {})
    }
}
