package com.degree.homedash.shared.di

import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.network.HaWebSocketClient
import org.koin.dsl.module

/** Koin module exposing the shared HA core. */
val sharedModule = module {
    single { HaWebSocketClient() }
    single { HaRepository(get()) }
}
