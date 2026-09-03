package com.degree.homedash.core.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.core.device.ThermostatDeviceUi
import com.degree.homedash.core.util.AppThermostatTapGuard
import com.degree.homedash.core.util.ControlPreview
import com.degree.homedash.core.util.FanIcon
import com.degree.homedash.core.util.PillButton
import com.degree.homedash.core.util.ThermostatConfirmationWindow
import com.degree.homedash.core.util.ThermostatTapGuard
import com.degree.homedash.core.util.previewThermostat
import com.degree.homedash.shared.model.device_metadata.HvacAction
import com.degree.homedash.shared.model.device_metadata.HvacMode
import com.degree.homedash.shared.model.device_metadata.PresetKind
import com.degree.homedash.shared.model.device_metadata.TemperaturePreset
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.dewPoint
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.formatNumber
import com.degree.homedash.ui.icons.ControlIcons
import kotlin.math.abs
import kotlin.math.round
import kotlinx.coroutines.delay

/** Pills per selector row. Fixed so the card's height can be computed rather than measured. */
private const val ModesPerRow = 4

/** How long a burst of −/+ taps has to settle before one `set_temperature` goes out. */
private const val SetTemperatureDebounceMs = 700L

/**
 * How long an optimistic value waits for Home Assistant to echo it before giving up. A call made
 * while the socket is down is dropped silently (see `HaWebSocketClient.callService`), so without
 * this the card would show a setpoint no thermostat ever agreed to, indefinitely.
 */
private const val EchoTimeoutMs = 8_000L

/** Setpoints are Doubles off the wire; compare with a tolerance rather than for equality. */
private const val TemperatureEpsilon = 0.01

/**
 * Thermostat card. Always full-width (see `cardSpan`) with everything visible: the current status,
 * a −/+ setpoint stepper over the ambient reading, and a pill row per selector the thermostat's
 * [ThermostatMetadata] declares.
 *
 * Taps are optimistic — the readout and pills move immediately and are reconciled when Home
 * Assistant reports back — because a round trip takes long enough that a card driven purely by
 * projected state feels broken.
 */
@Composable
internal fun ThermostatControl(
    ui: ThermostatDeviceUi,
    onSetTarget: (Double) -> Unit,
    onSetHvacMode: (HvacMode) -> Unit,
    onSetFanMode: (String) -> Unit,
    onSetPresetMode: (String) -> Unit,
    onSetExtreme: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tapGuard: ThermostatTapGuard = AppThermostatTapGuard,
) {
    val metadata = ui.metadata
    val enabled = !ui.offline

    // The action a confirmation is currently standing in front of, or null when nothing is pending.
    // Holding the action itself rather than a description of it keeps the guard out of the business
    // of knowing what the card's controls do.
    var awaitingConfirmation by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Every control on the card goes through this. Inside the window a tap behaves exactly as it did
    // before; outside it, the tap is parked until the dialog comes back.
    //
    // Gating here — at the tap — rather than at the outgoing service call is deliberate: the controls
    // are optimistic, so gating the call would let the readout jump to a value the user then declines,
    // and snap back seconds later.
    val guarded: (() -> Unit) -> Unit = { action ->
        if (tapGuard.isConfirmed(ui.id)) action() else awaitingConfirmation = action
    }

    awaitingConfirmation?.let { action ->
        ThermostatConfirmationDialog(
            displayName = ui.name,
            onConfirm = {
                awaitingConfirmation = null
                tapGuard.confirm(ui.id)
                action()
            },
            onDismiss = { awaitingConfirmation = null },
        )
    }

    // Optimistic mode selections. No debounce, unlike the setpoint: each is a single deliberate tap,
    // not a burst, so there is nothing to coalesce.
    var pendingHvacMode by remember { mutableStateOf<HvacMode?>(null) }
    var pendingFanMode by remember { mutableStateOf<String?>(null) }
    var pendingPreset by remember { mutableStateOf<PresetKind?>(null) }

    // Any change HA reports settles the tap, whether it agreed with us or not — HA always wins.
    LaunchedEffect(ui.hvacMode) { pendingHvacMode = null }
    LaunchedEffect(ui.fanMode) { pendingFanMode = null }
    // …and give up if it never reports one, so a dropped call can't strand the highlight.
    LaunchedEffect(pendingHvacMode) {
        if (pendingHvacMode != null) { delay(EchoTimeoutMs); pendingHvacMode = null }
    }
    LaunchedEffect(pendingFanMode) {
        if (pendingFanMode != null) { delay(EchoTimeoutMs); pendingFanMode = null }
    }

    val shownHvacMode = pendingHvacMode ?: ui.hvacMode
    val shownFanMode = pendingFanMode ?: ui.fanMode
    val tint = statusTint(ui.hvacAction, shownHvacMode)

    val presets = metadata.temperaturePresets

    // Which preset the thermostat is *actually* sitting on. Derived from the setpoint rather than
    // stored, so it stays right when the temperature is changed from the HA app or the unit itself.
    val target = ui.targetTemperature
    val activePreset = presets.firstOrNull { preset ->
        val setpoint = preset.setpointFor(shownHvacMode, ui.extremeActive)
        setpoint != null && target != null && abs(setpoint - target) < TemperatureEpsilon
    }
    // A tapped preset fills straight away rather than waiting on the round trip, and gives way as
    // soon as Home Assistant reports a setpoint.
    LaunchedEffect(ui.targetTemperature) { pendingPreset = null }
    LaunchedEffect(pendingPreset) {
        if (pendingPreset != null) { delay(EchoTimeoutMs); pendingPreset = null }
    }
    val shownPreset = pendingPreset ?: activePreset?.kind

    val presetOptions = presets.map { preset ->
        ModeOption(
            label = null,
            color = preset.kind.tone.color,
            isOn = preset.kind == shownPreset,
            icon = preset.kind.icon,
            contentDescription = preset.kind.name,
        )
    } + listOfNotNull(
        metadata.extremeToggle?.let {
            ModeOption(
                label = null,
                color = AppColors.HvacExtreme,
                isOn = ui.extremeActive,
                icon = ControlIcons.PresetExtreme,
                contentDescription = "Extreme temperatures",
            )
        },
    )

    // The plain Surface rather than HomeDashboardCard: the card body isn't tappable — every control
    // is its own target — and ClimateControl already takes this route for the same reason.
    //
    // Height wraps the content rather than being computed from the option counts. The card is always
    // alone on its grid row (see `cardSpan`), so nothing needs it to be a predictable size — and a
    // formula silently clips the bottom row the moment the spacing or the content changes.
    Surface(
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = AppColors.CardBackground,
        shadowElevation = Dimens.CardElevation,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = ControlIcons.Thermostat,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(Dimens.RowIconSize),
                )
                Text(
                    text = ui.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = statusText(ui),
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    maxLines = 1,
                )
                FanIcon(
                    spinning = isFanSpinning(ui),
                    tint = tint,
                    durationMs = 1500,
                    modifier = Modifier.size(24.dp)
                )
            }

            metadata.targetTemperature?.let { range ->
                TargetTemperatureStepper(
                    target = ui.targetTemperature,
                    range = range,
                    unitLabel = metadata.unitLabel,
                    currentTemperature = ui.currentTemperature,
                    currentHumidity = ui.currentHumidity,
                    fahrenheit = metadata.fahrenheit,
                    tint = tint,
                    enabled = enabled,
                    onSet = onSetTarget,
                    guarded = guarded,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (metadata.hvacModes.isNotEmpty()) {
                LabeledSelectorRow(
                    label = "Mode",
                    // Each mode pill carries its own tone, so Heat reads warm and Cool reads cool
                    // whichever one is currently selected.
                    options = metadata.hvacModes.map {
                        // Nothing fills when HA reports a mode we don't offer — heat_cool set from
                        // the HA app, say — rather than lying about which pill is active.
                        ModeOption(it.label, it.tone.color, isOn = it == shownHvacMode, icon = it.icon)
                    },
                    enabled = enabled,
                ) { index ->
                    guarded {
                        val mode = metadata.hvacModes[index]
                        pendingHvacMode = mode
                        onSetHvacMode(mode)
                    }
                }
            }

            if (metadata.fanModes.isNotEmpty()) {
                LabeledSelectorRow(
                    label = "Fan",
                    options = metadata.fanModes.map {
                        ModeOption(it.modeLabel(), tint, isOn = it == shownFanMode)
                    },
                    enabled = enabled,
                ) { index ->
                    guarded {
                        val mode = metadata.fanModes[index]
                        pendingFanMode = mode
                        onSetFanMode(mode)
                    }
                }
            }

            if (presets.isNotEmpty() || metadata.extremeToggle != null) {
                LabeledSelectorRow(
                    label = "Preset",
                    options = presetOptions,
                    // The temperature pills need a mode to resolve against; Extreme doesn't, but a
                    // row that half-disables reads as broken, so the whole row goes together.
                    enabled = enabled && shownHvacMode.resolvesPresets(),
                ) { index ->
                    guarded {
                        if (index in presets.indices) {
                            val preset = presets[index]
                            preset.setpointFor(shownHvacMode, ui.extremeActive)?.let { setpoint ->
                                pendingPreset = preset.kind
                                onSetTarget(setpoint)
                            }
                        } else {
                            val nowExtreme = !ui.extremeActive
                            onSetExtreme(nowExtreme)
                            // Toggling doesn't just change what the pills *would* write — the setpoint
                            // in force moves with them, so a thermostat sitting on Comfort follows
                            // Comfort to its new value instead of silently falling off the preset.
                            // Only on a tap here: reacting to the helper changing would have every open
                            // dashboard write the same setpoint at once.
                            activePreset?.let { preset ->
                                preset.setpointFor(shownHvacMode, nowExtreme)?.let { setpoint ->
                                    pendingPreset = preset.kind
                                    onSetTarget(setpoint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The confirmation standing in front of every thermostat control.
 *
 * A dialog rather than an undo affordance after the fact: the whole point is that a heating system
 * shouldn't be told to do anything at all by a sleeve brushing a wall tablet, and "undo" only helps
 * someone who noticed.
 *
 * It names the thermostat and the window, so the second half of the bargain — that this is the only
 * question the user will be asked for a while — is visible before they agree to it.
 */
@Composable
private fun ThermostatConfirmationDialog(
    displayName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = ControlIcons.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(Dimens.RowIconSize),
            )
        },
        // The name goes in the question rather than the body: the roster labels this one just
        // "Thermostat", which reads as a sentence in "Adjust Thermostat?" and as a typo in "This
        // will change Thermostat."
        title = { Text("Adjust $displayName?") },
        text = {
            Text(
                "The change goes through right away, and you won't be asked again for " +
                    "${ThermostatConfirmationWindow.inWholeMinutes} minutes.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Adjust") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * −/+ setpoint stepper. Taps move the readout immediately via the optimistic [pending] value while
 * the `climate.set_temperature` call is debounced, so a burst of five taps produces one call.
 *
 * [pending] is deliberately *not* keyed on [target] — the idiom [FanSpeedSlider] uses. A slider
 * survives that because a drag resolves in one gesture, but here every unrelated state push (an
 * ambient-temperature tick, `hvac_action` flipping to "cooling") would throw the pending value away
 * mid-burst and the readout would jump backwards under the user's finger.
 */
@Composable
private fun TargetTemperatureStepper(
    target: Double?,
    range: ThermostatMetadata.TargetTemperature,
    unitLabel: String,
    currentTemperature: Double?,
    currentHumidity: Double?,
    fahrenheit: Boolean,
    tint: Color,
    enabled: Boolean,
    onSet: (Double) -> Unit,
    guarded: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pending by remember { mutableStateOf<Double?>(null) }
    // The setpoint we sent. Lets the reconcile below tell HA's reply apart from an echo of the value
    // it already held, which arrives first on some integrations.
    var sent by remember { mutableStateOf<Double?>(null) }
    val onSetLatest by rememberUpdatedState(onSet)

    // Restarts on every tap, so only the last value of a burst is ever sent.
    LaunchedEffect(pending) {
        val value = pending ?: return@LaunchedEffect
        delay(SetTemperatureDebounceMs)
        sent = value
        onSetLatest(value)
        delay(EchoTimeoutMs)
        pending = null
        sent = null
    }

    // Runs only when the *projected* setpoint actually changes — the whole point of not keying
    // `remember` on it above.
    LaunchedEffect(target) {
        val optimistic = pending ?: return@LaunchedEffect
        val confirmed = target
        val agreed = confirmed != null && abs(confirmed - optimistic) < TemperatureEpsilon
        // Either HA landed on our value, or it moved somewhere else after our call went out — it
        // clamped us, rejected us, or another client got there first. Either way, HA wins.
        if (agreed || sent != null) {
            pending = null
            sent = null
        }
    }

    val shown = pending ?: target
    val decimals = if (range.step < 1.0) 1 else 0
    val canStep = enabled && shown != null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Filled.Remove, "Lower target temperature", tint, canStep) {
            guarded { pending = stepTo(shown, -range.step, range) }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = shown?.let { "${formatNumber(it, decimals)}$unitLabel" } ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            NowReadout(
                currentTemperature = currentTemperature,
                currentHumidity = currentHumidity,
                unitLabel = unitLabel,
                fahrenheit = fahrenheit,
            )
        }
        StepperButton(Icons.Filled.Add, "Raise target temperature", tint, canStep) {
            guarded { pending = stepTo(shown, +range.step, range) }
        }
    }
}

/**
 * The ambient line under the setpoint: what the room is actually like right now, as against the
 * target above it. Reads `now 🌡 71° / 💧 55° (59%)`.
 *
 * Dew point rather than bare humidity as the headline number, because it's the one that says how the
 * air will *feel* without having to be read against the temperature — 59% means something different
 * at 71° than at 80°. The relative humidity trails it in parentheses since that's the figure the
 * thermostat itself reports and the rest of the dashboard shows.
 *
 * Both readings are dropped silently when the thermostat doesn't report humidity (most don't) rather
 * than showing dashes for something that will never arrive.
 */
@Composable
private fun NowReadout(
    currentTemperature: Double?,
    currentHumidity: Double?,
    unitLabel: String,
    fahrenheit: Boolean,
) {
    val dew = if (currentTemperature != null && currentHumidity != null) {
        dewPoint(currentTemperature, currentHumidity, fahrenheit)
    } else {
        null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        // Fixed, so the card doesn't change height when a reading drops out.
        modifier = Modifier.height(Dimens.InlineIconSize + 4.dp),
    ) {
        Text(
            text = if (currentTemperature != null) "now" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        currentTemperature?.let { temperature ->
            NowReading(
                icon = Icons.Filled.Thermostat,
                tint = AppColors.TempWarm,
                contentDescription = "Current temperature",
                text = "${formatNumber(temperature, 1)}$unitLabel",
            )
            if (dew != null && currentHumidity != null) {
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
                NowReading(
                    icon = Icons.Filled.Opacity,
                    tint = AppColors.Wet,
                    contentDescription = "Dew point",
                    text = "${formatNumber(dew, 0)}$unitLabel (${formatNumber(currentHumidity, 0)}%)",
                )
            }
        }
    }
}

/** One glyph-and-number pair on the [NowReadout] line. */
@Composable
private fun NowReading(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    text: String,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(Dimens.InlineIconSize),
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * One step from [from], snapped back onto the step grid before clamping — a unit reporting 71.5 with
 * a 1° step would otherwise stay off-grid forever.
 */
private fun stepTo(
    from: Double?,
    delta: Double,
    range: ThermostatMetadata.TargetTemperature,
): Double {
    val base = from ?: range.min
    val snapped = round((base + delta) / range.step) * range.step
    return snapped.coerceIn(range.min, range.max)
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) tint else tint.copy(alpha = 0.35f),
        ),
        modifier = Modifier.size(Dimens.StepperButtonSize),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                },
            )
        }
    }
}

/**
 * One choice in a selector row.
 *
 * [isOn] sits on the option rather than the row holding a selected index, because the preset row
 * isn't a radio group: its three temperature pills are mutually exclusive but Extreme fills
 * independently of them. A null [label] is an icon-only pill.
 */
private data class ModeOption(
    val label: String?,
    val color: Color,
    val isOn: Boolean,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
)

/** A caption plus its pills, so an "On/Off" row can't be mistaken for the mode row next to it. */
@Composable
private fun LabeledSelectorRow(
    label: String,
    options: List<ModeOption>,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        // FlowRow with a fixed cap makes wrapping deterministic, which is what lets the card's
        // height be computed from the option counts instead of measured.
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            maxItemsInEachRow = ModesPerRow,
        ) {
            options.forEachIndexed { index, option ->
                PillButton(
                    text = option.label,
                    isOn = option.isOn,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f),
                    icon = option.icon,
                    iconSize = Dimens.PillIconSize,
                    color = option.color,
                    enabled = enabled,
                    contentDescription = option.contentDescription,
                )
            }
        }
    }
}

/**
 * What a mode or an action *means* to the eye, and the single place that meaning becomes a colour.
 *
 * [HvacMode] and [HvacAction] overlap in meaning but not in values — only one of them can be sent to
 * Home Assistant, and `idle`/`heat_cool` exist on one side each — so they stay separate types. This
 * is the one thing they genuinely share, factored out so the palette isn't spelled twice.
 */
private enum class HvacTone(val color: Color) {
    Warm(AppColors.TempWarm),
    Cool(AppColors.TempCool),
    Damp(AppColors.Wet),
    Air(AppColors.FanBlue),
    Neutral(AppColors.StatusGray),

    /** Preset-only tones — no hvac mode or action maps to these. */
    Comfort(AppColors.HvacComfort),
    Night(AppColors.HvacNight),
    Thrift(AppColors.HvacEco),
}

private val HvacMode.tone: HvacTone
    get() = when (this) {
        HvacMode.Heat -> HvacTone.Warm
        HvacMode.Cool -> HvacTone.Cool
        HvacMode.Dry -> HvacTone.Damp
        HvacMode.FanOnly -> HvacTone.Air
        // Genuinely ambiguous until hvac_action says which way it's driving; warm is the safer read
        // of a mode whose whole point is that it might heat.
        HvacMode.HeatCool, HvacMode.Auto -> HvacTone.Warm
        HvacMode.Off -> HvacTone.Neutral
    }

/** null is "no opinion": an idle unit isn't doing anything, so the mode it's set to decides. */
private val HvacAction.tone: HvacTone?
    get() = when (this) {
        HvacAction.Heating, HvacAction.Preheating, HvacAction.Defrosting -> HvacTone.Warm
        HvacAction.Cooling -> HvacTone.Cool
        HvacAction.Drying -> HvacTone.Damp
        HvacAction.Fan -> HvacTone.Air
        HvacAction.Off -> HvacTone.Neutral
        HvacAction.Idle -> null
    }

/**
 * What the card tints itself by: the live [action] while the unit is actually doing something,
 * otherwise the [mode] it's set to.
 *
 * Idle deliberately falls through to the mode rather than going grey. A thermostat is idle most of
 * the time, and a card that greys out between cycles reads as switched off — the "Idle" badge is
 * what carries that, while the colour says what the thermostat is *for*. Grey is reserved for a
 * unit that really is off or offline.
 */
private fun statusTint(action: HvacAction?, mode: HvacMode?): Color =
    (action?.tone ?: mode?.tone ?: HvacTone.Neutral).color

/** The badge in the card's top-right: what it's doing, or failing that what it's set to. */
private fun statusText(ui: ThermostatDeviceUi): String {
    val action = ui.hvacAction
    val mode = ui.hvacMode
    return when {
        ui.offline -> "Offline"
        action != null -> action.label
        mode != null -> mode.label
        else -> "—"
    }
}

/** The badge in the card's top-right: what it's doing, or failing that what it's set to. */
private fun isFanSpinning(ui: ThermostatDeviceUi): Boolean = when (ui.hvacAction) {
    null, HvacAction.Off, HvacAction.Idle -> false
    else -> true
}

private val HvacMode.label: String
    get() = when (this) {
        HvacMode.Off -> "Off"
        HvacMode.Heat -> "Heat"
        HvacMode.Cool -> "Cool"
        HvacMode.HeatCool -> "Heat/Cool"
        HvacMode.Auto -> "Auto"
        HvacMode.Dry -> "Dry"
        HvacMode.FanOnly -> "Fan"
    }

/**
 * True for the modes a preset can resolve a setpoint in.
 *
 * Mirrors [TemperaturePreset.setpointFor] returning null everywhere else — `off` has no target, and
 * `heat_cool`/`auto` drive a low/high pair no single setpoint can express.
 */
private fun HvacMode?.resolvesPresets(): Boolean = this == HvacMode.Heat || this == HvacMode.Cool

/** Preset pills are icon-only, so the glyph is the entire label. */
private val PresetKind.icon: ImageVector
    get() = when (this) {
        PresetKind.Comfort -> ControlIcons.PresetComfort
        PresetKind.Sleep -> ControlIcons.PresetSleep
        PresetKind.Economy -> ControlIcons.PresetEconomy
    }

private val PresetKind.tone: HvacTone
    get() = when (this) {
        PresetKind.Comfort -> HvacTone.Comfort
        PresetKind.Sleep -> HvacTone.Night
        PresetKind.Economy -> HvacTone.Thrift
    }

/**
 * The glyph on a mode's pill, where one carries its meaning on its own.
 *
 * null for the compound and less common modes: `heat_cool`/`auto` have no single honest symbol, and
 * a wrong glyph is worse than none when the label is right there beside it.
 */
private val HvacMode.icon: ImageVector?
    get() = when (this) {
        HvacMode.Off -> ControlIcons.HvacOff
        HvacMode.Heat -> ControlIcons.HvacHeat
        HvacMode.Cool -> ControlIcons.HvacCool
        HvacMode.HeatCool, HvacMode.Auto, HvacMode.Dry, HvacMode.FanOnly -> null
    }

private val HvacAction.label: String
    get() = when (this) {
        HvacAction.Off -> "Off"
        HvacAction.Idle -> "Idle"
        HvacAction.Heating -> "Heating"
        HvacAction.Cooling -> "Cooling"
        HvacAction.Drying -> "Drying"
        HvacAction.Fan -> "Fan"
        HvacAction.Preheating -> "Preheating"
        HvacAction.Defrosting -> "Defrosting"
    }

/** Vendor mode strings are lowercase snake_case; title-case them for the pills. */
private fun String.modeLabel(): String =
    split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun ThermostatControlPreview() =
    ControlPreview {
        // The Living Room thermostat as Home Assistant actually reports it. 72° in cool is Comfort's
        // cool setpoint, so that pill fills — the match is derived, never stored.
        ThermostatControl(
            previewThermostat("Comfort"),
            {},
            {},
            {},
            {},
            {})
        // 69° in cool is Sleep's, so the same card fills a different pill on the strength of the
        // temperature alone.
        ThermostatControl(
            previewThermostat(
                "Sleep",
                target = 69.0
            ), {}, {}, {}, {}, {})
        // Heating: the presets resolve against their heat setpoints instead, and 60° is Economy's.
        ThermostatControl(
            previewThermostat(
                "Economy heating",
                mode = HvacMode.Heat,
                action = HvacAction.Heating,
                target = 60.0
            ),
            {}, {}, {}, {}, {},
        )
        // A setpoint matching no preset — nothing fills rather than guessing at the nearest.
        ThermostatControl(
            previewThermostat(
                "Off preset",
                target = 73.0
            ), {}, {}, {}, {}, {})
    }

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun ThermostatControlExtremePreview() =
    ControlPreview {
        // Extreme on: its pill fills, and the three presets now resolve against their extreme pairs —
        // 75° is Comfort's extreme cool setpoint, where 72° would have been its normal one.
        ThermostatControl(
            previewThermostat(
                "Extreme comfort",
                extremeActive = true,
                target = 75.0
            ),
            {}, {}, {}, {}, {},
        )
        // The same 72° that reads as Comfort above matches nothing once Extreme is on.
        ThermostatControl(
            previewThermostat(
                "Extreme, no match",
                extremeActive = true,
                target = 72.0
            ),
            {}, {}, {}, {}, {},
        )
        // A thermostat with presets but no helper entity: three pills, no Extreme.
        ThermostatControl(
            previewThermostat(
                "No extreme toggle",
                hasExtremeToggle = false,
                target = 70.0
            ),
            {}, {}, {}, {}, {},
        )
    }

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun ThermostatControlEdgeCasePreview() =
    ControlPreview {
        // Offline: readouts dash out and every control disables.
        ThermostatControl(
            previewThermostat(
                "Offline",
                offline = true
            ), {}, {}, {}, {}, {})
        // heat_cool set from the Home Assistant app — a mode the roster deliberately doesn't offer.
        // No pill highlights and the setpoint is absent, because HA publishes a low/high pair instead.
        ThermostatControl(
            previewThermostat(
                "Heat/Cool",
                mode = HvacMode.HeatCool,
                action = HvacAction.Idle,
                target = null
            ),
            {}, {}, {}, {}, {},
        )
        // Setpoint only — no selector rows at all, so the card is at its shortest.
        ThermostatControl(
            previewThermostat(
                "Setpoint only",
                hvacModes = emptyList(),
                fanModes = emptyList(),
                presetModes = emptyList(),
                temperaturePresets = emptyList(),
                hasExtremeToggle = false,
            ),
            {}, {}, {}, {}, {},
        )
        // The office heater's shape: two modes, three fan modes, no presets of either kind.
        ThermostatControl(
            previewThermostat(
                "Office Heater",
                mode = HvacMode.Heat,
                action = HvacAction.Heating,
                hvacModes = listOf(HvacMode.Off, HvacMode.Heat),
                fanModes = listOf("auto", "low", "high"),
                fanMode = "low",
                presetModes = emptyList(),
                temperaturePresets = emptyList(),
                hasExtremeToggle = false,
            ),
            {}, {}, {}, {}, {},
        )
        // Six modes: proves the FlowRow wrap and the height formula agree.
        ThermostatControl(
            previewThermostat(
                "Six modes",
                hvacModes = listOf(
                    HvacMode.Off, HvacMode.Heat, HvacMode.Cool,
                    HvacMode.HeatCool, HvacMode.Auto, HvacMode.FanOnly,
                ),
            ),
            {}, {}, {}, {}, {},
        )
    }
