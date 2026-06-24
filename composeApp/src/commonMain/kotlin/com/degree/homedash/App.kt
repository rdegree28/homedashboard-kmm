package com.degree.homedash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.degree.homedash.office.OfficeScreen
import com.degree.homedash.shared.data.HaConfig
import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.network.HaWebSocketClient

@Composable
fun App(config: HaConfig?) {
    val repository = remember { HaRepository(HaWebSocketClient()) }
    LaunchedEffect(config) {
        if (config != null) repository.connect(config)
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (config == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Not configured — set ha.url and ha.token in local.properties",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                OfficeScreen(repository)
            }
        }
    }
}
