package com.degree.homedash.office

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun DoorRowPreview() = ControlPreview {
    DoorRow("Open", previewEntity("on"))
    DoorRow("Closed", previewEntity("off"))
    DoorRow("Unavailable", previewEntity("unavailable"))
}
