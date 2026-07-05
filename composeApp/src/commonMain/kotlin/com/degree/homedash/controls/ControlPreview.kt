package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.degree.homedash.office.FanUi
import com.degree.homedash.office.ToggleUi

// Shared sample data + scaffolding for the control previews (which live next to each composable).

/** Wraps control previews in the app's dark theme + a padded column. */
@Composable
internal fun ControlPreview(content: @Composable ColumnScope.() -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content,
            )
        }
    }
}

internal fun previewToggle(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
) = ToggleUi(name = name, isOn = isOn, offline = offline)

internal fun previewFan(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
) = FanUi(name = name, isOn = isOn, offline = offline, levelCount = levelCount, percentage = percentage)
