package com.unyieldingmight.app.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.PasswordField
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.digitsOnly
import com.unyieldingmight.shared.domain.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val container: AppContainer) : ViewModel() {
    var email by mutableStateOf("")
    var code by mutableStateOf("")
    var password by mutableStateOf("")
    var codeSent by mutableStateOf(false)
    var showErrors by mutableStateOf(false)
    val loading = MutableStateFlow(false)

    fun sendCode(onDone: () -> Unit = {}) {
        showErrors = true
        if (Validation.email(email) != null) return
        run({ container.api.forgotPassword(email) }) {
            codeSent = true
            showErrors = false
            container.messenger.info("Check your email", it.message)
            onDone()
        }
    }

    fun reset(onReset: () -> Unit) {
        showErrors = true
        if (Validation.otp(code) != null || Validation.password(password) != null) return
        run({ container.api.resetPassword(email, code, password) }) {
            container.messenger.success("Password reset", it.message)
            onReset()
        }
    }

    private fun <T> run(block: suspend () -> T, onSuccess: (T) -> Unit) {
        viewModelScope.launch {
            loading.value = true
            try {
                onSuccess(block())
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                loading.value = false
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(windowSize: WindowSizeClass, onDone: () -> Unit) {
    val vm = containerViewModel { ForgotPasswordViewModel(it) }
    val loading by vm.loading.collectAsState()

    if (!vm.codeSent) {
        AuthLayout(windowSize, "Reset password", "Enter your account email and we'll send you a 6-digit code.", onBack = onDone) {
            UmTextField(
                vm.email, { vm.email = it.trim() }, "Email",
                error = if (vm.showErrors) Validation.email(vm.email) else null,
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Done, onDone = { vm.sendCode() }, leadingIcon = Icons.Rounded.Email,
            )
            PrimaryButton("Send code", { vm.sendCode() }, Modifier.fillMaxWidth(), loading = loading)
        }
    } else {
        AuthLayout(windowSize, "Choose a new password", "Enter the code sent to ${vm.email}.", onBack = { vm.codeSent = false }) {
            UmTextField(
                vm.code, { vm.code = it.digitsOnly(6) }, "6-digit code",
                error = if (vm.showErrors) Validation.otp(vm.code) else null, keyboardType = KeyboardType.NumberPassword, leadingIcon = Icons.Rounded.Pin,
            )
            PasswordField(
                vm.password, { vm.password = it }, "New password",
                error = if (vm.showErrors) Validation.password(vm.password) else null, imeAction = ImeAction.Done, onDone = { vm.reset(onDone) },
            )
            PrimaryButton("Reset password", { vm.reset(onDone) }, Modifier.fillMaxWidth(), loading = loading)
            TextButton(onClick = { vm.sendCode() }, enabled = !loading) { Text("Send a new code") }
        }
    }
}
