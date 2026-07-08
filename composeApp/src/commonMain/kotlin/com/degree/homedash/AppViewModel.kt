package com.degree.homedash

import androidx.lifecycle.ViewModel
import com.degree.homedash.shared.data.AuthDao
import com.degree.homedash.shared.data.AuthUser
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.data.HaConfig
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.data.Users
import com.degree.homedash.shared.network.HaWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped holder for the single Home Assistant connection and the persisted config. Owning these
 * in a ViewModel (rather than in composition) keeps the WebSocket alive across recomposition and
 * hands the same [repository] to every screen ViewModel.
 */
class AppViewModel(
    defaultConfig: HaConfig? = null,
    private val configStore: ConfigStore = ConfigStore(),
    private val authDao: AuthDao = AuthDao(),
    val repository: HomeAssistantRepo = HomeAssistantRepo(HaWebSocketClient()),
) : ViewModel() {

    private val _config = MutableStateFlow(configStore.load() ?: defaultConfig)
    val config: StateFlow<HaConfig?> = _config.asStateFlow()

    private val _currentUser = MutableStateFlow(authDao.load())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        _config.value?.let { repository.connect(it) }
    }

    /** Persist a new config and (re)connect the repository. */
    fun save(config: HaConfig) {
        configStore.save(config)
        _config.value = config
        repository.connect(config)
    }

    /** Validate [pin] for [user]; on success persist the session and return true. */
    fun login(user: AuthUser, pin: String): Boolean {
        if (!Users.validate(user, pin)) return false
        authDao.save(user)
        _currentUser.value = user
        return true
    }

    /** Clear the persisted session. */
    fun logout() {
        authDao.clear()
        _currentUser.value = null
    }
}
