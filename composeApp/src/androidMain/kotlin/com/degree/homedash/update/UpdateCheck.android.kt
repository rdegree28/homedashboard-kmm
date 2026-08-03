package com.degree.homedash.update

/** Android ships as an APK, so there is no deploy to poll for. */
actual suspend fun fetchDeployedBuildStamp(): String? = null

actual fun reloadApp() = Unit
