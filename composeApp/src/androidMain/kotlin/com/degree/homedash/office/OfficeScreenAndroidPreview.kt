package com.degree.homedash.office

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.degree.homedash.shared.network.ConnectionStatus

/** Android Studio preview pane renders this androidx @Preview (in an Android source set). */
@Preview(showBackground = true, widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent(
                states = previewStates,
                connection = ConnectionStatus.Connected,
                powerHistory = previewHistory,
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                onSetFanSpeed = { _, _ -> },
                onSignal = {},
            )
        }
    }
}
