package com.unyieldingmight.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.unyieldingmight.app.data.SessionState
import com.unyieldingmight.app.ui.auth.AuthNavHost
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.MessageHost
import com.unyieldingmight.app.ui.shell.MainShell

@Composable
fun UmApp(windowSize: WindowSizeClass) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsState()
    val expired by container.session.expired.collectAsState()

    LaunchedEffect(expired) {
        if (expired) {
            container.cart.clear()
            container.messenger.info("Session expired", "For your security you've been signed out. Please log in again.")
            container.session.acknowledgeExpiry()
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = session::class,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "session",
        ) { kind ->
            when (kind) {
                SessionState.SignedIn::class -> (session as? SessionState.SignedIn)?.let { MainShell(windowSize, it.user) }
                SessionState.SignedOut::class -> AuthNavHost(windowSize)
                else -> Box(Modifier.fillMaxSize())
            }
        }
        MessageHost(container.messenger)
    }
}
