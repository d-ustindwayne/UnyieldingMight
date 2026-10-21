package com.unyieldingmight.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.unyieldingmight.app.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val LocalContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer not provided") }

/** Creates a ViewModel with access to the app's dependencies. */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(key: String? = null, crossinline create: (AppContainer) -> VM): VM {
    val container = LocalContainer.current
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}

/** Whether the signed-in user currently has an active membership. */
@Composable
fun rememberIsMember(): Boolean {
    val container = LocalContainer.current
    val user by container.session.user.collectAsState(container.session.currentUser)
    return user?.isMember == true
}

/** Loading / loaded / failed state for a screen's data. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val data: T, val refreshing: Boolean = false) : Load<T>
    data class Failed(val message: String) : Load<Nothing>

    val dataOrNull: T? get() = (this as? Ready<T>)?.data
}

/** Base for screens that load one thing from the API and can refresh it. */
abstract class LoadViewModel<T>(protected val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<Load<T>>(Load.Loading)
    val state: StateFlow<Load<T>> = _state.asStateFlow()
    private var job: Job? = null

    protected abstract suspend fun load(): T

    fun refresh() {
        job?.cancel()
        val previous = _state.value.dataOrNull
        _state.value = if (previous != null) Load.Ready(previous, refreshing = true) else Load.Loading
        job = viewModelScope.launch {
            _state.value = try {
                Load.Ready(load())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (previous != null) {
                    container.messenger.error(e)
                    Load.Ready(previous)
                } else Load.Failed(e.message ?: "Couldn't load this right now.")
            }
        }
    }

    protected fun setData(data: T) {
        _state.value = Load.Ready(data)
    }

    /** Runs an action, reporting failures in the message modal. Returns null on failure. */
    protected fun <R> act(busy: MutableStateFlow<Boolean>? = null, block: suspend () -> R, onSuccess: (R) -> Unit = {}) {
        viewModelScope.launch {
            busy?.value = true
            try {
                onSuccess(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                busy?.value = false
            }
        }
    }
}
