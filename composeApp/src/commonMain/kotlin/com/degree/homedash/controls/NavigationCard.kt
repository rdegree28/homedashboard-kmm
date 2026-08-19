package com.degree.homedash.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degree.homedash.shared.model.entity.NavigationMetadata
import com.degree.homedash.shared.model.entity.NavigationMetadata.NavigationTarget
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.icons.cardPhoto
import com.degree.homedash.ui.icons.roomIcon
import com.degree.homedash.ui.roomTint
import org.jetbrains.compose.resources.painterResource

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
            CardArt(metadata = ui.metadata, glyphSize = 40.dp)
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

/**
 * A launcher card's leading art: the photograph it declares, or its glyph when it declares none.
 *
 * A photo is cropped to a circle and ringed in the card's tint, so it reads as a portrait rather
 * than a picture wedged into an icon slot — and so the tile keeps the colour that a glyph got from
 * being tinted. It draws larger than a glyph because a face needs the area a silhouette doesn't.
 */
@Composable
private fun CardArt(
    metadata: NavigationMetadata,
    glyphSize: Dp,
    modifier: Modifier = Modifier,
    photoSize: Dp = 48.dp,
) {
    val tint = roomTint(metadata.tint)
    val photo = metadata.photo
    if (photo == null) {
        Icon(
            imageVector = roomIcon(metadata.icon),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(glyphSize),
        )
    } else {
        Image(
            painter = painterResource(cardPhoto(photo)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            // Order matters: the clip has to precede both the fill and the ring so they follow the
            // circle, and the ring is drawn after the image (Border strokes post-content) so an
            // opaque photo can't hide it. The tinted disc shows through only until the bitmap
            // arrives — on web the painter is empty for the first frames, and a ring around nothing
            // reads as a rendering bug.
            modifier = modifier
                .size(photoSize)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, tint, CircleShape),
        )
    }
}

/**
 * The grid form of a launcher card, for the Home screen's 2-column layout. Reuses [EntityCard] so a
 * dashboard tile sits at the same size and weight as the light and fan tiles elsewhere; the label
 * wraps to two lines, which "Living Room" needs at half width.
 */
@Composable
fun NavigationTile(
    ui: EntityUi.Navigation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeDashboardCard(
        onClick = onClick,
        enabled = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.TopCenter),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CardArt(
                metadata = ui.metadata,
                glyphSize = 38.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = ui.displayName,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                fontWeight = FontWeight.SemiBold,
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

/** The 2-column form, as Home lays it out. */
@Preview(widthDp = 380)
@Composable
private fun NavigationTilePreview() = ControlPreview {
    NavigationTarget.entries.toList().chunked(2).forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pair.forEach { target ->
                Box(Modifier.weight(1f)) {
                    NavigationTile(previewNavigation(target), onClick = {}, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
