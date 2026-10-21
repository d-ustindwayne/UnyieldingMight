package com.unyieldingmight.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unyieldingmight.app.ui.theme.ThemeMode
import com.unyieldingmight.shared.model.AppConfigDto
import com.unyieldingmight.shared.model.AuthResponse
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("unyielding_might")

enum class UnitSystem { METRIC, IMPERIAL }

/** Device-local preferences: theme and measurement units. */
class Preferences(private val context: Context, scope: CoroutineScope) {
    private val themeKey = stringPreferencesKey("theme")
    private val unitsKey = stringPreferencesKey("units")

    val theme: StateFlow<ThemeMode> = context.dataStore.data
        .map { prefs -> prefs[themeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM }
        .stateIn(scope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val units: StateFlow<UnitSystem> = context.dataStore.data
        .map { prefs -> prefs[unitsKey]?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() } ?: UnitSystem.METRIC }
        .stateIn(scope, SharingStarted.Eagerly, UnitSystem.METRIC)

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[themeKey] = mode.name }
    suspend fun setUnits(units: UnitSystem) = context.dataStore.edit { it[unitsKey] = units.name }
}

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: UserDto) : SessionState
}

/**
 * Owns the auth token and the signed-in user. The token is persisted so the app reopens signed in;
 * the user profile is always re-fetched so role or membership changes show up immediately.
 */
class SessionManager(private val context: Context, private val scope: CoroutineScope) {
    private val tokenKey = stringPreferencesKey("token")

    @Volatile
    private var token: String? = null

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _config = MutableStateFlow<AppConfigDto?>(null)
    val config: StateFlow<AppConfigDto?> = _config.asStateFlow()

    /** Set when the server rejects our token so the UI can explain why the user was signed out. */
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    val api = UmApi(tokenProvider = { token }, onUnauthorized = { scope.launch { signOut(expired = true) } })

    val user: Flow<UserDto?> = state.map { (it as? SessionState.SignedIn)?.user }
    val currentUser: UserDto? get() = (state.value as? SessionState.SignedIn)?.user

    fun restore() {
        scope.launch { runCatching { _config.value = api.config() } }
        scope.launch {
            token = context.dataStore.data.first()[tokenKey]
            if (token == null) {
                _state.value = SessionState.SignedOut
                return@launch
            }
            try {
                _state.value = SessionState.SignedIn(api.me())
            } catch (e: ApiException) {
                // Offline: stay signed out of the UI but keep the token so the next launch can retry.
                if (e.isUnauthorized) signOut() else _state.value = SessionState.SignedOut
            }
        }
    }

    suspend fun signIn(auth: AuthResponse) {
        token = auth.token
        context.dataStore.edit { it[tokenKey] = auth.token }
        _expired.value = false
        _state.value = SessionState.SignedIn(auth.user)
        if (_config.value == null) runCatching { _config.value = api.config() }
    }

    fun updateUser(user: UserDto) {
        if (_state.value is SessionState.SignedIn) _state.value = SessionState.SignedIn(user)
    }

    suspend fun refreshUser() {
        runCatching { api.me() }.onSuccess { updateUser(it) }
    }

    suspend fun signOut(expired: Boolean = false) {
        token = null
        context.dataStore.edit { it.remove(tokenKey) }
        _expired.value = expired
        _state.value = SessionState.SignedOut
    }

    fun acknowledgeExpiry() {
        _expired.value = false
    }
}
