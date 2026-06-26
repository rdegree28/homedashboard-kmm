package com.degree.homedash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/** Android Studio preview pane renders this androidx @Preview (in an Android source set). */
@Preview(showBackground = true, widthDp = 380, heightDp = 400)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(onOpenOffice = {}, onOpenPlants = {}, onOpenSettings = {})
        }
    }
}
