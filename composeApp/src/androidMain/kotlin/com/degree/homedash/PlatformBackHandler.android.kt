package com.degree.homedash

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/** Routes the Android system back button to the app's back stack when there's somewhere to go. */
@Composable
actual fun PlatformBackHandler(backStackSize: Int, onBack: () -> Unit) {
    BackHandler(enabled = backStackSize > 1, onBack = onBack)
}
