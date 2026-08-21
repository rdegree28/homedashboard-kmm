package com.degree.homedash.shared.di

import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.dao.FeatureFlagDaoStaticImpl
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.api.HaClient
import com.degree.homedash.shared.api.HaWebSocketClient
import com.degree.homedash.shared.api.HomeAssistantActionApi
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.api.HomeAssistantStateApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantActionApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantStateApi
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import org.koin.dsl.module

/**
 * Koin module exposing the shared HA core. The app's ViewModels are declared separately in
 * `:composeApp`, which this module knows nothing about.
 */
val sharedModule = module {
    single<HaClient> { HaWebSocketClient() }
    single<HomeAssistantApi> { WebSocketHomeAssistantApi(get()) }
    single<HomeAssistantActionApi> { WebSocketHomeAssistantActionApi(get()) }
    single<HomeAssistantStateApi> { WebSocketHomeAssistantStateApi(get()) }
    single { HomeAssistantRepo(get()) }
    single { ExpHomeAssistantRepo(get(), get()) }
    single { EntityMetadataRepo(get(), get()) }
    single { ConfigStore() }
    // AuthRepo's constructor is internal (it wraps the private AuthDao), so build it via its factory.
    single { AuthRepo.create() }
    single<FeatureFlagDao> { FeatureFlagDaoStaticImpl() }
}
