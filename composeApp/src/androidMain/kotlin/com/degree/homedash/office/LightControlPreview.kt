package com.degree.homedash.office

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun LightControlPreview() = ControlPreview {
    LightControl("On", previewEntity("on"), Icons.Filled.Lightbulb) {}
    LightControl("Off", previewEntity("off"), Icons.Filled.Lightbulb) {}
    LightControl("Offline", previewEntity("unavailable"), Icons.Filled.Lightbulb) {}
}
