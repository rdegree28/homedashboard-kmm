package com.degree.homedash.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A pressable tile that fires a scene, script or automation.
 *
 * There is no on/off state to show — pressing it is a one-shot action — so instead of reflecting
 * state the play icon animates on press: it slides the width of the row, then springs back to full
 * size where it started. See [playLaunchAnimation].
 */
@Composable
fun TriggerCard(
    ui: EntityUi.Trigger,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val slide = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val fade = remember { Animatable(1f) }
    var animation by remember { mutableStateOf<Job?>(null) }

    // Measured so the icon travels the full row rather than a hardcoded distance.
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val iconWidthPx = with(LocalDensity.current) { Dimens.RowIconSize.toPx() }

    HomeDashboardCard(
        onClick = {
            // Fire the scene first; the animation is feedback, never a gate on the action.
            onActivate()
            // A press mid-flight still activates but doesn't restart the animation, which would
            // otherwise leave the icon stranded mid-slide.
            if (animation?.isActive != true) {
                animation = scope.launch { playLaunchAnimation(slide, scale, fade) }
            }
        },
        enabled = true,
        modifier = modifier,
        height = Dimens.SmallEntityCardHeight,
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().onSizeChanged { rowWidthPx = it.width },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = AppColors.PlayBlue,
                modifier = Modifier
                    .size(Dimens.RowIconSize)
                    // graphicsLayer, not offset: this moves the icon without re-laying out the row,
                    // so the label stays put while the icon travels over it.
                    .graphicsLayer {
                        translationX = slide.value * (rowWidthPx - iconWidthPx).coerceAtLeast(0f)
                        scaleX = scale.value
                        scaleY = scale.value
                        alpha = fade.value
                    },
            )
            Text(
                modifier = Modifier.weight(1f),
                text = ui.displayName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * The press animation: the icon accelerates across the row shrinking as it goes, fades out over the
 * last stretch so the travel itself stays visible, then reappears at its starting point and springs
 * back up to full size.
 *
 * [slide] is a 0..1 fraction of the row rather than a pixel distance, so the caller can scale it to
 * whatever width the card ended up.
 */
private suspend fun playLaunchAnimation(
    slide: Animatable<Float, AnimationVector1D>,
    scale: Animatable<Float, AnimationVector1D>,
    fade: Animatable<Float, AnimationVector1D>,
) {
    // All three run together, and all must land before the reset — otherwise a snapTo below can
    // cancel a still-running animation on the same Animatable mid-flight.
    coroutineScope {
        launch { scale.animateTo(0.6f, tween(durationMillis = SLIDE_MS)) }
        launch { fade.animateTo(0f, tween(durationMillis = FADE_MS, delayMillis = SLIDE_MS - FADE_MS)) }
        launch { slide.animateTo(1f, tween(durationMillis = SLIDE_MS, easing = FastOutLinearInEasing)) }
    }

    // Back to the start, invisible and small, before growing into place.
    slide.snapTo(0f)
    scale.snapTo(0f)
    fade.snapTo(1f)
    scale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
}

private const val SLIDE_MS = 340

/** Fade runs at the tail of the slide, so most of the travel is visible. */
private const val FADE_MS = 130

@Preview(widthDp = 380)
@Composable
private fun TriggerCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) {
            TriggerCard(previewTrigger("Main Lights"), onActivate = {}, modifier = Modifier.fillMaxWidth())
        }
        Box(Modifier.weight(1f)) {
            TriggerCard(previewTrigger("Movie Night"), onActivate = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
