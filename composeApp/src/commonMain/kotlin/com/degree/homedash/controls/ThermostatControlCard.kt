package com.degree.homedash.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.degree.homedash.shared.model.entity.HvacAction
import com.degree.homedash.shared.model.entity.HvacMode
import com.degree.homedash.shared.model.entity.ThermostatMetadata
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.formatNumber
import com.degree.homedash.ui.icons.ControlIcons
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round

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
 * Thermostat card. Always full-width (see [cardSpan]) with everything visible: the current status,
 * a −/+ setpoint stepper over the ambient reading, and a pill row per selector the thermostat's
 * [ThermostatMetadata] declares.
 *
 * Taps are optimistic — the readout and pills move immediately and are reconciled when Home
 * Assistant reports back — because a round trip takes long enough that a card driven purely by
 * projected state feels broken.
 */
@Composable
internal fun ThermostatControlCard(
    ui: EntityUi.Thermostat,
    onSetTarget: (Double) -> Unit,
    onSetHvacMode: (HvacMode) -> Unit,
    onSetFanMode: (String) -> Unit,
    onSetPresetMode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadata = ui.metadata
    val enabled = !ui.offline

    // Optimistic mode selections. No debounce, unlike the setpoint: each is a single deliberate tap,
    // not a burst, so there is nothing to coalesce.
    var pendingHvacMode by remember { mutableStateOf<HvacMode?>(null) }
    var pendingFanMode by remember { mutableStateOf<String?>(null) }
    var pendingPresetMode by remember { mutableStateOf<String?>(null) }

    // Any change HA reports settles the tap, whether it agreed with us or not — HA always wins.
    LaunchedEffect(ui.hvacMode) { pendingHvacMode = null }
    LaunchedEffect(ui.fanMode) { pendingFanMode = null }
    LaunchedEffect(ui.presetMode) { pendingPresetMode = null }
    // …and give up if it never reports one, so a dropped call can't strand the highlight.
    LaunchedEffect(pendingHvacMode) {
        if (pendingHvacMode != null) { delay(EchoTimeoutMs); pendingHvacMode = null }
    }
    LaunchedEffect(pendingFanMode) {
        if (pendingFanMode != null) { delay(EchoTimeoutMs); pendingFanMode = null }
    }
    LaunchedEffect(pendingPresetMode) {
        if (pendingPresetMode != null) { delay(EchoTimeoutMs); pendingPresetMode = null }
    }

    val shownHvacMode = pendingHvacMode ?: ui.hvacMode
    val shownFanMode = pendingFanMode ?: ui.fanMode
    val shownPresetMode = pendingPresetMode ?: ui.presetMode
    val tint = statusTint(ui.hvacAction, shownHvacMode)

    val selectorRows = rowsFor(metadata.hvacModes.size) +
        rowsFor(metadata.fanModes.size) +
        rowsFor(metadata.presetModes.size)

    // The plain Surface rather than HomeDashboardCard: the card body isn't tappable — every control
    // is its own target — and ClimateCard already takes this route for the same reason.
    Surface(
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = AppColors.CardBackground,
        shadowElevation = Dimens.CardElevation,
        modifier = modifier.height(
            Dimens.ThermostatCardHeight + Dimens.ThermostatSelectorRowHeight * selectorRows,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimens.EntityCardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                    text = ui.displayName,
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
            }

            metadata.targetTemperature?.let { range ->
                TargetTemperatureStepper(
                    target = ui.targetTemperature,
                    range = range,
                    unitLabel = metadata.unitLabel,
                    currentTemperature = ui.currentTemperature,
                    tint = tint,
                    enabled = enabled,
                    onSet = onSetTarget,
                )
            }

            if (metadata.hvacModes.isNotEmpty()) {
                LabeledSelectorRow(
                    label = "Mode",
                    options = metadata.hvacModes.map { it.label },
                    // -1 when HA reports a mode we don't offer — heat_cool set from the HA app, say.
                    // Nothing highlights, rather than lying about which pill is active.
                    selectedIndex = metadata.hvacModes.indexOf(shownHvacMode),
                    tint = tint,
                    enabled = enabled,
                ) { index ->
                    val mode = metadata.hvacModes[index]
                    pendingHvacMode = mode
                    onSetHvacMode(mode)
                }
            }

            if (metadata.fanModes.isNotEmpty()) {
                LabeledSelectorRow(
                    label = "Fan",
                    options = metadata.fanModes.map { it.modeLabel() },
                    selectedIndex = metadata.fanModes.indexOf(shownFanMode),
                    tint = tint,
                    enabled = enabled,
                ) { index ->
                    val mode = metadata.fanModes[index]
                    pendingFanMode = mode
                    onSetFanMode(mode)
                }
            }

            if (metadata.presetModes.isNotEmpty()) {
                LabeledSelectorRow(
                    label = "Preset",
                    options = metadata.presetModes.map { it.modeLabel() },
                    selectedIndex = metadata.presetModes.indexOf(shownPresetMode),
                    tint = tint,
                    enabled = enabled,
                ) { index ->
                    val mode = metadata.presetModes[index]
                    pendingPresetMode = mode
                    onSetPresetMode(mode)
                }
            }
        }
    }
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
    tint: Color,
    enabled: Boolean,
    onSet: (Double) -> Unit,
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
            pending = stepTo(shown, -range.step, range)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = shown?.let { "${formatNumber(it, decimals)}$unitLabel" } ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                // Non-breaking space, not "", so the row keeps its height when there's no reading.
                text = currentTemperature?.let { "now ${formatNumber(it, 1)}$unitLabel" } ?: " ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StepperButton(Icons.Filled.Add, "Raise target temperature", tint, canStep) {
            pending = stepTo(shown, +range.step, range)
        }
    }
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

/** A caption plus its pills, so an "On/Off" row can't be mistaken for the mode row next to it. */
@Composable
private fun LabeledSelectorRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    tint: Color,
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
                    text = option,
                    isOn = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f),
                    color = tint,
                    enabled = enabled,
                )
            }
        }
    }
}

/** Rows a selector of [count] options occupies; 0 when it has none, so it costs no height. */
private fun rowsFor(count: Int): Int = ceil(count / ModesPerRow.toFloat()).toInt()

/**
 * What the card tints itself by: the live [action] while the unit is actually doing something,
 * otherwise the [mode] it's set to.
 *
 * Idle deliberately falls through to the mode rather than going grey. A thermostat is idle most of
 * the time, and a card that greys out between cycles reads as switched off — the "Idle" badge is
 * what carries that, while the colour says what the thermostat is *for*. Grey is reserved for a
 * unit that really is off or offline.
 */
private fun statusTint(action: HvacAction?, mode: HvacMode?): Color = when (action) {
    HvacAction.Heating, HvacAction.Preheating, HvacAction.Defrosting -> AppColors.TempWarm
    HvacAction.Cooling -> AppColors.TempCool
    HvacAction.Drying -> AppColors.Wet
    HvacAction.Fan -> AppColors.FanBlue
    HvacAction.Off -> AppColors.StatusGray
    HvacAction.Idle, null -> when (mode) {
        HvacMode.Heat -> AppColors.TempWarm
        HvacMode.Cool -> AppColors.TempCool
        HvacMode.Dry -> AppColors.Wet
        HvacMode.FanOnly -> AppColors.FanBlue
        HvacMode.HeatCool, HvacMode.Auto -> AppColors.TempWarm
        HvacMode.Off, null -> AppColors.StatusGray
    }
}

/** The badge in the card's top-right: what it's doing, or failing that what it's set to. */
private fun statusText(ui: EntityUi.Thermostat): String = when {
    ui.offline -> "Offline"
    ui.hvacAction != null -> ui.hvacAction.label
    ui.hvacMode != null -> ui.hvacMode.label
    else -> "—"
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
private fun ThermostatControlCardPreview() = ControlPreview {
    // The Living Room thermostat as Home Assistant actually reports it.
    ThermostatControlCard(previewThermostat("Thermostat"), {}, {}, {}, {})
    ThermostatControlCard(
        previewThermostat("Heating", mode = HvacMode.Heat, action = HvacAction.Heating, target = 74.0),
        {}, {}, {}, {},
    )
    ThermostatControlCard(
        previewThermostat("Off", mode = HvacMode.Off, action = HvacAction.Off),
        {}, {}, {}, {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun ThermostatControlCardEdgeCasePreview() = ControlPreview {
    // Offline: readouts dash out and every control disables.
    ThermostatControlCard(previewThermostat("Offline", offline = true), {}, {}, {}, {})
    // heat_cool set from the Home Assistant app — a mode the roster deliberately doesn't offer.
    // No pill highlights and the setpoint is absent, because HA publishes a low/high pair instead.
    ThermostatControlCard(
        previewThermostat("Heat/Cool", mode = HvacMode.HeatCool, action = HvacAction.Idle, target = null),
        {}, {}, {}, {},
    )
    // Setpoint only — no selector rows, so the card is at its shortest.
    ThermostatControlCard(
        previewThermostat("Setpoint only", hvacModes = emptyList(), fanModes = emptyList(), presetModes = emptyList()),
        {}, {}, {}, {},
    )
    // The office heater's shape: two modes, three fan modes, no presets.
    ThermostatControlCard(
        previewThermostat(
            "Office Heater",
            mode = HvacMode.Heat,
            action = HvacAction.Heating,
            hvacModes = listOf(HvacMode.Off, HvacMode.Heat),
            fanModes = listOf("auto", "low", "high"),
            fanMode = "low",
            presetModes = emptyList(),
        ),
        {}, {}, {}, {},
    )
    // Six modes: proves the FlowRow wrap and the height formula agree.
    ThermostatControlCard(
        previewThermostat(
            "Six modes",
            hvacModes = listOf(
                HvacMode.Off, HvacMode.Heat, HvacMode.Cool,
                HvacMode.HeatCool, HvacMode.Auto, HvacMode.FanOnly,
            ),
        ),
        {}, {}, {}, {},
    )
}
