package com.degree.homedash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.model.FeatureFlag
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-scoped holder for the single Home Assistant connection and the persisted config. Owning these
 * in a ViewModel (rather than in composition) keeps the WebSocket alive across recomposition and
 * hands the same [repository] to every screen ViewModel.
 */
class AppViewModel(
    defaultConfig: HaConfig?,
    private val configStore: ConfigStore,
    private val authRepo: AuthRepo,
    private val featureFlagDao: FeatureFlagDao,
    private val repository: ExpHomeAssistantRepo,
) : ViewModel() {

    private val _config = MutableStateFlow(configStore.load() ?: defaultConfig)
    val config: StateFlow<HaConfig?> = _config.asStateFlow()

    /** The logged-in user, driven reactively by [AuthRepo] (persisted across launches). */
    val currentUser: StateFlow<AuthUser?> = authRepo.loadCurrentUser()

    /** The selectable users for the login screen. */
    val users: List<AuthUser> = authRepo.loadAllUserNames().map(::AuthUser)

    /** Feature flags enabled for the current user (none when logged out); recomputed on login/logout. */
    val featureFlags: StateFlow<Set<FeatureFlag>> =
        currentUser
            .map { flagsFor(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, flagsFor(currentUser.value))

    private fun flagsFor(user: AuthUser?): Set<FeatureFlag> =
        user?.let(featureFlagDao::getFeatureFlagsForUser).orEmpty()

    init {
        _config.value?.let { repository.connect(it) }
    }

    /** Persist a new config and (re)connect the repository. */
    fun save(config: HaConfig) {
        configStore.save(config)
        _config.value = config
        repository.connect(config)
    }

    /** Validate [pin] for [user]; on success persist the session (which updates [currentUser]) and return true. */
    fun login(user: AuthUser, pin: String): Boolean {
        if (!authRepo.validate(user.name, pin)) return false
        viewModelScope.launch { authRepo.onUserLoggedIn(user.name) }
        return true
    }

    /** Clear the persisted session. */
    fun logout() {
        viewModelScope.launch { authRepo.onUserLoggedOut() }
    }
}
