package com.degree.homedash.shared.di

import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.dao.FeatureFlagDaoStaticImpl
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.api.HaClient
import com.degree.homedash.shared.api.HaWebSocketClient
import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.api.WebSocketExpHomeAssistantApi
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import org.koin.dsl.module

/**
 * Koin module exposing the shared HA core. The app's ViewModels are declared separately in
 * `:composeApp`, which this module knows nothing about.
 */
val sharedModule = module {
    single<HaClient> { HaWebSocketClient() }
    single<ExpHomeAssistantApi> { WebSocketExpHomeAssistantApi(get()) }
    single { ExpHomeAssistantRepo(get()) }
    single { EntityMetadataRepo(get(), get()) }
    single { ConfigStore() }
    // AuthRepo's constructor is internal (it wraps the private AuthDao), so build it via its factory.
    single { AuthRepo.create() }
    single<FeatureFlagDao> { FeatureFlagDaoStaticImpl() }
}
