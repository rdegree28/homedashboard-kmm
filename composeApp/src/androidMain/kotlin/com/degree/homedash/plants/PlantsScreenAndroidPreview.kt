package com.degree.homedash.plants

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/** Android Studio preview pane renders this androidx @Preview (in an Android source set). */
@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun PlantsScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlantsContent(plants = previewPlants, onBack = {}, onOpenSettings = {})
        }
    }
}
