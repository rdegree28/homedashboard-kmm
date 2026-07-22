package com.degree.homedash.shared.di

import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.api.HaClient
import com.degree.homedash.shared.api.HaWebSocketClient
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantApi
import org.koin.dsl.module

/** Koin module exposing the shared HA core. */
val sharedModule = module {
    single<HaClient> { HaWebSocketClient() }
    single<HomeAssistantApi> { WebSocketHomeAssistantApi(get()) }
    single { HomeAssistantRepo(get()) }
    single { ConfigStore() }
}
