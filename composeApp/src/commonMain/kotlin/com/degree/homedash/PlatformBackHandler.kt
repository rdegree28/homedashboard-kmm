package com.degree.homedash

import androidx.compose.runtime.Composable

/**
 * Bridges the app's in-memory back stack to the platform's native "back" gesture:
 * the Android system back button and the web browser's Back button.
 *
 * [backStackSize] is the current depth (1 == root). When it is > 1 a back press should
 * pop one level by invoking [onBack]; the actual keeps the platform history in sync so the
 * native control and the in-app back arrow stay balanced.
 */
@Composable
expect fun PlatformBackHandler(backStackSize: Int, onBack: () -> Unit)
