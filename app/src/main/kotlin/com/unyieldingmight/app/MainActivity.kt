package com.unyieldingmight.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.unyieldingmight.app.data.SessionState
import com.unyieldingmight.app.ui.UmApp
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.theme.UnyieldingMightTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val container = (application as UnyieldingMightApp).container
        val splash = installSplashScreen()
        // Keep the splash up while the saved session is being restored, so there's no login-screen flash.
        splash.setKeepOnScreenCondition { container.session.state.value is SessionState.Loading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by container.preferences.theme.collectAsState()
            val windowSize = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalContainer provides container) {
                UnyieldingMightTheme(theme) {
                    UmApp(windowSize)
                }
            }
        }
    }
}
