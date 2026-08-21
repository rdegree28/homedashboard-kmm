package com.degree.homedash.di

import com.degree.homedash.AppViewModel
import com.degree.homedash.HomeViewModel
import com.degree.homedash.bedroom.BedroomViewModel
import com.degree.homedash.livingroom.LivingRoomViewModel
import com.degree.homedash.office.OfficeViewModel
import com.degree.homedash.pets.PetsViewModel
import com.degree.homedash.pets.WaterGraphViewModel
import com.degree.homedash.plants.PlantGraphViewModel
import com.degree.homedash.plants.PlantsViewModel
import com.degree.homedash.shared.api.HaConfig
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The app's ViewModels. Separate from `sharedModule` because ViewModels live here in `:composeApp`,
 * which `:shared` can't see.
 *
 * A function rather than a `val` so it can close over [defaultConfig] — the platform-supplied config
 * (Android `BuildConfig`, web `WebDefaults`) that seeds the connection before Settings is visited.
 * It's fixed for the life of the process.
 *
 * The graph ViewModels take their entity id as a runtime parameter, supplied by the screen with
 * `parametersOf(entityId)`.
 */
fun appModule(defaultConfig: HaConfig?) = module {
    viewModel { AppViewModel(defaultConfig, get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { OfficeViewModel(get(), get()) }
    viewModel { LivingRoomViewModel(get(), get(),get()) }
    viewModel { BedroomViewModel(get(), get()) }
    viewModel { PetsViewModel(get(), get()) }
    viewModel { PlantsViewModel(get()) }
    viewModel { (entityId: String) -> PlantGraphViewModel(get(), entityId) }
    viewModel { (entityId: String) -> WaterGraphViewModel(get(), entityId, get()) }
}
