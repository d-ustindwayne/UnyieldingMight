package com.unyieldingmight.app.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.ui.theme.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiMessage(
    val kind: Kind,
    val title: String,
    val text: String,
    val action: String = "OK",
    val onDismiss: (() -> Unit)? = null,
) {
    enum class Kind { INFO, SUCCESS, ERROR }
}

/**
 * App-wide queue of informational, status and error messages, shown one at a time as a small modal
 * (replacing the original app's mix of Toasts and the full-screen ModalActivity).
 */
class Messenger {
    private val _queue = MutableStateFlow<List<UiMessage>>(emptyList())
    val current: StateFlow<List<UiMessage>> = _queue.asStateFlow()

    fun show(message: UiMessage) = _queue.update { it + message }

    fun info(title: String, text: String, onDismiss: (() -> Unit)? = null) = show(UiMessage(UiMessage.Kind.INFO, title, text, onDismiss = onDismiss))
    fun success(title: String, text: String, onDismiss: (() -> Unit)? = null) = show(UiMessage(UiMessage.Kind.SUCCESS, title, text, onDismiss = onDismiss))
    fun error(text: String, title: String = "Something went wrong") = show(UiMessage(UiMessage.Kind.ERROR, title, text))
    fun error(throwable: Throwable, title: String = "Something went wrong") =
        error(throwable.message ?: "Please try again.", title)

    fun dismiss() {
        val head = _queue.value.firstOrNull() ?: return
        _queue.update { it.drop(1) }
        head.onDismiss?.invoke()
    }
}

/**
 * Draws the current message as an overlay inside the app's own layout rather than a separate dialog
 * window, so it can't get lost behind screen transitions (e.g. right after signing in).
 */
@Composable
fun MessageHost(messenger: Messenger) {
    val queue by messenger.current.collectAsState()
    val message = queue.firstOrNull()

    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
        // Keep showing the last message while the exit animation runs.
        val shown = remember { mutableStateOf<UiMessage?>(null) }
        if (message != null) shown.value = message
        val current = shown.value ?: return@AnimatedVisibility

        BackHandler(onBack = messenger::dismiss)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = messenger::dismiss)
                .semantics { paneTitle = current.title },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 360.dp)
                    // Swallow taps on the card so only the scrim dismisses.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val (icon, tint) = when (current.kind) {
                        UiMessage.Kind.INFO -> Icons.Rounded.Info to MaterialTheme.colorScheme.primary
                        UiMessage.Kind.SUCCESS -> Icons.Rounded.CheckCircle to Success
                        UiMessage.Kind.ERROR -> Icons.Rounded.ErrorOutline to MaterialTheme.colorScheme.error
                    }
                    Surface(shape = CircleShape, color = tint.copy(alpha = 0.14f)) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(12.dp).size(28.dp))
                    }
                    Text(current.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(
                        current.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = messenger::dismiss, modifier = Modifier.fillMaxWidth()) { Text(current.action) }
                }
            }
        }
    }
}
