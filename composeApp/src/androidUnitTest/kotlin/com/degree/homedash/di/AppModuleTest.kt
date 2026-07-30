package com.degree.homedash.di

import com.degree.homedash.plants.PlantGraphViewModel
import com.degree.homedash.plants.PlantsViewModel
import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.di.sharedModule
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.test.verify.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Guards the DI graph. A missing binding compiles fine and only blows up when the screen is opened,
 * so these stand in for launching the app.
 */
class AppModuleTest {

    // ViewModels build their state flows on viewModelScope, which needs a main dispatcher.
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /**
     * The ViewModels that can be built off-device resolve and construct from the real two-module graph.
     *
     * Only Plants and its graph screen qualify. Everything else — Home, Office, Living Room, Pets,
     * WaterGraph, and AppViewModel — reaches `EntityMetadataRepo` or `ConfigStore`, both of which lead
     * to multiplatform-settings' no-arg factory and so need a real Android context. Covering those here
     * would mean adding Robolectric; until then their wiring is checked by
     * [appModuleDefinitionsAreSatisfiable], which never instantiates anything.
     */
    @Test
    fun contextFreeViewModelsResolve() {
        val koin = koinApplication { modules(sharedModule, appModule(defaultConfig = null)) }.koin

        assertNotNull(koin.get<PlantsViewModel>())
        // Takes its entity id as a runtime parameter.
        assertNotNull(koin.get<PlantGraphViewModel> { parametersOf("sensor.louie_moisture_sensor_soil_moisture") })
    }

    /**
     * Reflection-only check that every constructor parameter of every definition in [appModule] is
     * accounted for — the one way to cover `AppViewModel` without building it.
     *
     * `verify` only sees the module it's called on, so everything `sharedModule` provides is declared
     * as externally supplied, along with [HaConfig] (captured by [appModule], not injected) and
     * [String] (the graph screens' runtime entity id).
     */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModuleDefinitionsAreSatisfiable() {
        appModule(defaultConfig = null).verify(
            extraTypes = listOf(
                HaConfig::class,
                String::class,
                ConfigStore::class,
                AuthRepo::class,
                FeatureFlagDao::class,
                HomeAssistantRepo::class,
                EntityMetadataRepo::class,
            ),
        )
    }

    // sharedModule deliberately gets no verify() counterpart: several of its types take defaulted
    // constructor args (ConfigStore's Settings) or internal ones (AuthRepo's AuthDao, which this
    // module can't even name), and verify() reflects on constructors without seeing either. Its
    // bindings are covered for real by screenViewModelsResolve instead.
}
