package com.unyieldingmight.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.PasswordField
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.navigation.ForgotPasswordRoute
import com.unyieldingmight.app.ui.navigation.LoginRoute
import com.unyieldingmight.app.ui.navigation.RegisterRoute
import com.unyieldingmight.shared.domain.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun AuthNavHost(windowSize: WindowSizeClass) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(windowSize, onRegister = { nav.navigate(RegisterRoute) }, onForgot = { nav.navigate(ForgotPasswordRoute) })
        }
        composable<RegisterRoute> { RegisterScreen(windowSize, onExit = { nav.popBackStack() }) }
        composable<ForgotPasswordRoute> { ForgotPasswordScreen(windowSize, onDone = { nav.popBackStack() }) }
    }
}

class LoginViewModel(private val container: AppContainer) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var showErrors by mutableStateOf(false)
    val loading = MutableStateFlow(false)

    val emailError get() = if (showErrors) Validation.email(email) else null
    val passwordError get() = if (showErrors && password.isEmpty()) "Enter your password" else null

    fun login() {
        showErrors = true
        if (Validation.email(email) != null || password.isEmpty()) return
        viewModelScope.launch {
            loading.value = true
            try {
                container.session.signIn(container.api.login(email, password))
            } catch (e: Exception) {
                container.messenger.error(e, "Couldn't log in")
            } finally {
                loading.value = false
            }
        }
    }
}

@Composable
fun LoginScreen(windowSize: WindowSizeClass, onRegister: () -> Unit, onForgot: () -> Unit) {
    val vm = containerViewModel { LoginViewModel(it) }
    val loading by vm.loading.collectAsState()

    AuthLayout(windowSize, title = "Welcome back", subtitle = "Log in to book classes, shop and track your progress.") {
        UmTextField(
            vm.email, { vm.email = it.trim() }, "Email",
            error = vm.emailError, keyboardType = KeyboardType.Email, leadingIcon = Icons.Rounded.Email,
        )
        PasswordField(vm.password, { vm.password = it }, error = vm.passwordError, imeAction = ImeAction.Done, onDone = vm::login)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgot) { Text("Forgot your password?") }
        }
        PrimaryButton("Log in", vm::login, Modifier.fillMaxWidth(), loading = loading)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onRegister) { Text("Register now") }
        }
    }
}
