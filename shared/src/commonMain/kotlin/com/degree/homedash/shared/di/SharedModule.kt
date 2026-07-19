package com.degree.homedash.shared.di

import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.api.HaWebSocketClient
import org.koin.dsl.module

/** Koin module exposing the shared HA core. */
val sharedModule = module {
    single { HaWebSocketClient() }
    single { HomeAssistantRepo(get()) }
    single { ConfigStore() }
}
