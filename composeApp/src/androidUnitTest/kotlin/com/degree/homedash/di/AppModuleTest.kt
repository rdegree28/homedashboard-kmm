package com.degree.homedash.di

import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlin.test.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * Guards the DI graph. A missing binding compiles fine and only blows up when the screen is opened,
 * so these stand in for launching the app.
 */
class AppModuleTest {

    // No `contextFreeViewModelsResolve` counterpart any more: every ViewModel now reads its roster
    // from `EntityMetadataRepo`, which leads to multiplatform-settings' no-arg factory and so needs a
    // real Android context. Plants and PlantGraph were the last two that could be built off-device,
    // and they went the same way when they moved onto the roster. Restoring that check means adding
    // Robolectric; until then [appModuleDefinitionsAreSatisfiable] is the guard, and it never
    // instantiates anything.

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
    // module can't even name), and verify() reflects on constructors without seeing either.
}
