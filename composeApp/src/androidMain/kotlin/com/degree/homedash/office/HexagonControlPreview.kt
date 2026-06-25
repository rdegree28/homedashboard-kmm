package com.degree.homedash.office

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun HexagonControlPreview() = ControlPreview {
    HexagonControl("On", previewEntity("on")) {}
    HexagonControl("Off", previewEntity("off")) {}
    HexagonControl("Offline", previewEntity("unavailable")) {}
}
